package com.psi.sale.mq.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.async.facade.MqMessageFacade;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.feign.DocFeignClient;
import com.psi.common.feign.DocFeignResponse;
import com.psi.common.feign.DocFeignResponse.DocFeignItemResponse;
import com.psi.common.context.UserContext;
import com.psi.common.context.UserInfo;
import com.psi.common.idempotent.MessageIdempotencyService;
import com.psi.common.message.MessageFactory;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.result.CommonResult;
import com.psi.sale.dto.SaleOrderItemSaveDTO;
import com.psi.sale.dto.SaleOrderMainDTO;
import com.psi.sale.dto.SaleOrderSaveDTO;
import com.psi.sale.dto.SaleOutItemSaveDTO;
import com.psi.sale.dto.SaleOutMainDTO;
import com.psi.sale.dto.SaleOutSaveDTO;
import com.psi.sale.dto.SaleReturnItemSaveDTO;
import com.psi.sale.dto.SaleReturnMainDTO;
import com.psi.sale.dto.SaleReturnSaveDTO;
import com.psi.sale.entity.SaleOutSelfUseMainEntity;
import com.psi.sale.service.SaleOrderMainService;
import com.psi.sale.service.SaleOutMainService;
import com.psi.sale.service.SaleOutSelfUseMainService;
import com.psi.sale.service.SaleReturnMainService;
import com.psi.sale.service.CustomerPaymentService;
import com.psi.sale.dto.CustomerPaymentSaveDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 销售流程完成MQ监听器
 * 
 * <p>监听销售流程完成队列，当销售订单/销售出库/销售退货审批通过后，
 * 生成销售模块正式业务数据
 * 
 * <p>通过 Feign 远程调用 psi-flow 获取单据数据并更新状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SaleProcessCompletedListener {

    private final DocFeignClient docFeignClient;
    private final SaleOrderMainService saleOrderMainService;
    private final SaleOutMainService saleOutMainService;
    private final SaleOutSelfUseMainService saleOutSelfUseMainService;
    private final SaleReturnMainService saleReturnMainService;
    private final CustomerPaymentService customerPaymentService;
    private final MqMessageFacade mqMessageFacade;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final MessageIdempotencyService messageIdempotencyService;

    /**
     * 获取 SKU+业务单位 对应的换算率
     * 优先使用单据中保存的换算率（快照），为空则兜底查询
     */
    private BigDecimal getConversionRate(String skuCode, String unitCode, BigDecimal docRate) {
        if (docRate != null && docRate.compareTo(BigDecimal.ZERO) > 0) {
            return docRate;
        }
        if (skuCode == null || unitCode == null || unitCode.trim().isEmpty()) {
            return BigDecimal.ONE;
        }
        try {
            BigDecimal rate = jdbcTemplate.queryForObject(
                    "SELECT conversion_rate FROM psi_goods.goods_sku_sale_unit WHERE sku_code = ? AND symbol = ? LIMIT 1",
                    BigDecimal.class, skuCode, unitCode);
            if (rate != null && rate.compareTo(BigDecimal.ZERO) > 0) {
                return rate;
            }
        } catch (Exception e) {
            log.warn("查询SKU[{}]单位[{}]换算率失败: {}", skuCode, unitCode, e.getMessage());
        }
        log.warn("未找到SKU[{}]销售单位[{}]的换算率，按1:1处理", skuCode, unitCode);
        return BigDecimal.ONE;
    }

    /**
     * 根据商品编码查询默认 SKU 编码（用于前端未传 skuCode 时的兜底）
     */
    private String getDefaultSkuCode(String goodsCode) {
        if (goodsCode == null || goodsCode.trim().isEmpty()) {
            return null;
        }
        try {
            List<String> results = jdbcTemplate.queryForList(
                    "SELECT sku_code FROM psi_goods.goods_sku WHERE goods_code = ? LIMIT 1",
                    String.class, goodsCode);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            log.warn("查询商品默认SKU失败: goodsCode={}, error={}", goodsCode, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitMQConstant.PROCESS_COMPLETED_SALE_QUEUE)
    public void onProcessCompleted(MqCommonMessage<?> message) {
        Map<String, Object> data = (Map<String, Object>) message.getData();
        String bizId = (String) data.get("bizId");
        String messageId = message.getMessageId();
        Integer processStatus = data.get("processStatus") != null ? Integer.parseInt(data.get("processStatus").toString()) : 2;
        log.info("收到销售流程完成消息: bizId={}, processStatus={}, messageId={}", bizId, processStatus, messageId);

        messageIdempotencyService.execute(messageId, () -> {
            // 从 MQ 消息还原租户/操作人上下文，供 MyBatis 自动填充与租户拦截器使用
            UserInfo userInfo = new UserInfo();
            userInfo.setTenantId(message.getTenantId());
            userInfo.setUpdateUserId(message.getOperatorId());
            UserContext.set(userInfo);
            try {
                DocFeignResponse doc = resolveDocFromMessage(data, bizId);
                if (doc == null) {
                    return null;
                }

                // 流程驳回时释放预占库存，不生成正式业务单据
                if (processStatus != null && processStatus == 3) {
                    sendSaleOrderReleaseMessage(doc);
                    log.info("销售流程已驳回，预占库存释放消息已发送: bizId={}, docType={}", bizId, doc.getDocType());
                    return null;
                }

                if ("SALE_RETURN".equals(doc.getDocType())) {
                    saveSaleReturn(doc);
                } else if ("SALE_OUT".equals(doc.getDocType())) {
                    saveSaleOut(doc);
                } else {
                    saveSaleOrder(doc);
                }

                log.info("销售单据正式数据已生成: bizId={}, docType={}", bizId, doc.getDocType());
            } catch (Exception e) {
                log.error("处理销售流程完成消息失败: bizId={}, error={}", bizId, e.getMessage(), e);
                throw e;
            } finally {
                UserContext.clearAll();
            }
            return null;
        });
    }

    /**
     * 优先从 MQ 消息里取 docData（工作流已查好），没有则 Feign 查询（兼容老消息）
     */
    private DocFeignResponse resolveDocFromMessage(Map<String, Object> data, String bizId) {
        Object docData = data.get("docData");
        if (docData != null && !docData.toString().trim().isEmpty()) {
            try {
                DocFeignResponse doc = objectMapper.readValue(docData.toString(), DocFeignResponse.class);
                log.info("销售流程完成：从MQ消息中解析单据数据: bizId={}", bizId);
                return doc;
            } catch (Exception e) {
                log.warn("销售流程完成：MQ中docData解析失败，降级Feign查询: bizId={}, error={}", bizId, e.getMessage());
            }
        }

        CommonResult<DocFeignResponse> result = docFeignClient.findByDocNo(bizId);
        if (!result.isSuccess() || result.getData() == null) {
            log.warn("销售流程完成：单据不存在或查询失败: bizId={}, msg={}", bizId, result.getMessage());
            return null;
        }
        log.info("销售流程完成：通过Feign查询单据数据: bizId={}", bizId);
        return result.getData();
    }

    private Long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            log.warn("无法将字符串转换为Long: {}", value);
            return null;
        }
    }

    private void saveSaleOrder(DocFeignResponse doc) {
        Long customerId = parseLong(doc.getPartnerId());

        SaleOrderSaveDTO saveDTO = new SaleOrderSaveDTO();
        saveDTO.setOrderNo(doc.getDocNo());
        saveDTO.setDocName(doc.getDocName());
        saveDTO.setCustomerId(customerId);
        saveDTO.setCustomerCode(doc.getPartnerCode());
        saveDTO.setCustomerName(doc.getPartnerName());
        saveDTO.setSaleType(doc.getSaleType());
        saveDTO.setPaymentType(doc.getPaymentType());
        saveDTO.setCurrencyCode(doc.getCurrencyCode());
        saveDTO.setExchangeRate(doc.getExchangeRate());
        saveDTO.setWarehouseCode(doc.getWarehouseCode());
        saveDTO.setWarehouseName(doc.getWarehouseName());
        saveDTO.setRemark(doc.getRemark());

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        if (doc.getItems() != null && !doc.getItems().isEmpty()) {
            List<SaleOrderItemSaveDTO> items = new ArrayList<>();
            for (DocFeignItemResponse docItem : doc.getItems()) {
                SaleOrderItemSaveDTO item = new SaleOrderItemSaveDTO();
                item.setGoodsId(docItem.getGoodsId());
                item.setGoodsCode(docItem.getGoodsCode());
                String skuCode = docItem.getSkuCode() != null ? docItem.getSkuCode() : getDefaultSkuCode(docItem.getGoodsCode());
                item.setSkuCode(skuCode);
                item.setSkuName(docItem.getSkuName() != null ? docItem.getSkuName() : skuCode);
                item.setGoodsName(docItem.getGoodsName());
                item.setGoodsSpec(docItem.getGoodsSpec());
                item.setUnitCode(docItem.getGoodsUnit());
                item.setConversionRate(docItem.getConversionRate());
                item.setQuantity(docItem.getQuantity());
                item.setUnitPrice(docItem.getUnitPrice());
                item.setTaxRate(docItem.getTaxRate());
                item.setDiscountRate(docItem.getDiscountRate());
                items.add(item);
                if (docItem.getQuantity() != null && docItem.getUnitPrice() != null) {
                    calculatedTotal = calculatedTotal.add(docItem.getQuantity().multiply(docItem.getUnitPrice()));
                }
            }
            saveDTO.setItems(items);
        }

        BigDecimal totalAmount = doc.getTotalAmount() != null ? doc.getTotalAmount() : calculatedTotal;
        BigDecimal taxAmount = doc.getTaxAmount() != null ? doc.getTaxAmount() : BigDecimal.ZERO;
        BigDecimal discountAmount = doc.getDiscountAmount() != null ? doc.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal defaultPayAmount = totalAmount.add(taxAmount).subtract(discountAmount);

        // 如果 doc 中的 payAmount 为 null 或 0，则使用计算出的默认值
        BigDecimal effectivePayAmount = (doc.getPayAmount() != null && doc.getPayAmount().compareTo(BigDecimal.ZERO) > 0)
                ? doc.getPayAmount() : defaultPayAmount;

        saveDTO.setTotalAmount(totalAmount);
        saveDTO.setTaxAmount(taxAmount);
        saveDTO.setDiscountAmount(discountAmount);
        saveDTO.setPayAmount(effectivePayAmount);

        CommonResult<SaleOrderMainDTO> result = saleOrderMainService.save(saveDTO);
        if (result.isSuccess() && result.getData() != null) {
            saleOrderMainService.audit(result.getData().getId(), 1);

            CustomerPaymentSaveDTO paymentSaveDTO = new CustomerPaymentSaveDTO();
            paymentSaveDTO.setCustomerId(customerId);
            paymentSaveDTO.setCustomerCode(doc.getPartnerCode());
            paymentSaveDTO.setCustomerName(doc.getPartnerName());
            paymentSaveDTO.setPaymentDate(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            paymentSaveDTO.setPaymentAmount(effectivePayAmount);
            paymentSaveDTO.setPaymentMethod(doc.getPaymentType() != null ? doc.getPaymentType() : 1);
            paymentSaveDTO.setRemark("销售订单付款 - " + result.getData().getOrderNo());
            customerPaymentService.save(paymentSaveDTO);
            log.info("销售订单付款记录已生成: bizId={}, orderNo={}, payAmount={}",
                    doc.getDocNo(), result.getData().getOrderNo(), paymentSaveDTO.getPaymentAmount());
        }
        log.info("销售订单正式数据已生成: bizId={}, docNo={}", doc.getDocNo(), doc.getDocNo());
    }

    private void sendSaleOrderReleaseMessage(DocFeignResponse doc) {
        try {
            if (doc.getItems() == null || doc.getItems().isEmpty()) {
                return;
            }
            List<Map<String, Object>> itemList = new ArrayList<>();
            for (DocFeignItemResponse docItem : doc.getItems()) {
                Map<String, Object> itemMap = new HashMap<>();
                String skuCode = docItem.getSkuCode() != null ? docItem.getSkuCode() : getDefaultSkuCode(docItem.getGoodsCode());
                BigDecimal conversionRate = getConversionRate(skuCode, docItem.getGoodsUnit(), docItem.getConversionRate());
                BigDecimal stockQuantity = docItem.getQuantity().multiply(conversionRate);
                itemMap.put("skuCode", skuCode);
                itemMap.put("goodsCode", docItem.getGoodsCode());
                itemMap.put("quantity", docItem.getQuantity());
                itemMap.put("stockQuantity", stockQuantity);
                itemMap.put("unitCode", docItem.getGoodsUnit());
                itemMap.put("conversionRate", conversionRate);
                itemList.add(itemMap);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("orderNo", doc.getDocNo());
            data.put("warehouseCode", doc.getWarehouseCode());
            data.put("action", "RELEASE");
            data.put("items", itemList);
            MqCommonMessage<Map<String, Object>> message = MessageFactory.create(
                    data,
                    RabbitMQConstant.SALE_ORDER_EXCHANGE,
                    RabbitMQConstant.SALE_ORDER_RELEASE_ROUTING_KEY,
                    "SALE_ORDER_RELEASE_STOCK"
            );
            mqMessageFacade.sendAsync(message);
            log.info("销售订单释放库存消息已发送: orderNo={}", doc.getDocNo());
        } catch (Exception e) {
            log.error("发送销售订单释放库存消息失败: orderNo={}, error={}", doc.getDocNo(), e.getMessage(), e);
        }
    }

    private String normalizeExpireDate(String originalDate) {
        if (originalDate == null || originalDate.length() < 10) {
            return originalDate;
        }
        return originalDate.substring(0, 10);
    }

    private void saveSaleOut(DocFeignResponse doc) {
        // saleType=2 表示自用单（无销售订单直接出库）
        boolean selfUse = doc.getSaleType() != null && doc.getSaleType() == 2;
        if (selfUse) {
            saveSelfUseSaleOut(doc);
            return;
        }

        SaleOutSaveDTO saveDTO = new SaleOutSaveDTO();
        saveDTO.setOutNo(doc.getDocNo());
        saveDTO.setDocName(doc.getDocName());
        saveDTO.setCustomerCode(doc.getPartnerCode());
        saveDTO.setCustomerName(doc.getPartnerName());
        saveDTO.setWarehouseCode(doc.getWarehouseCode());
        saveDTO.setWarehouseName(doc.getWarehouseName());
        saveDTO.setOrderNo(doc.getOrderNo());
        saveDTO.setRemark(doc.getRemark());

        if (doc.getItems() != null && !doc.getItems().isEmpty()) {
            List<SaleOutItemSaveDTO> items = new ArrayList<>();
            for (DocFeignItemResponse docItem : doc.getItems()) {
                SaleOutItemSaveDTO item = new SaleOutItemSaveDTO();
                item.setGoodsId(docItem.getGoodsId());
                item.setGoodsCode(docItem.getGoodsCode());
                String skuCode = docItem.getSkuCode() != null ? docItem.getSkuCode() : getDefaultSkuCode(docItem.getGoodsCode());
                item.setSkuCode(skuCode);
                item.setSkuName(docItem.getSkuName() != null ? docItem.getSkuName() : skuCode);
                item.setGoodsName(docItem.getGoodsName());
                item.setGoodsSpec(docItem.getGoodsSpec());
                item.setUnitCode(docItem.getGoodsUnit());
                item.setConversionRate(docItem.getConversionRate());
                item.setOutQuantity(docItem.getQuantity());
                item.setUnitPrice(docItem.getUnitPrice());
                item.setTaxRate(docItem.getTaxRate());
                item.setOrderNo(doc.getOrderNo());
                item.setBatchNo(docItem.getBatchNo());
                item.setExpireDate(normalizeExpireDate(docItem.getExpiryDate()));
                items.add(item);
            }
            saveDTO.setItems(items);
        }

        CommonResult<SaleOutMainDTO> result = saleOutMainService.save(saveDTO);
        if (result.isSuccess() && result.getData() != null) {
            // 审批通过后直接设置为已审核状态(2)，触发库存扣减
            saleOutMainService.audit(result.getData().getId(), 2);
            // 发送销售出库财务消息，由 psi-finance 生成应收账款
            sendSaleOutFinanceMessage(result.getData(), doc.getPaymentType());
        }
        log.info("销售出库单正式数据已生成: bizId={}, docNo={}", doc.getDocNo());
    }

    private void saveSelfUseSaleOut(DocFeignResponse doc) {
        SaleOutSelfUseMainEntity main = saleOutSelfUseMainService.saveFromDraft(doc);
        // 自用单直接扣减可用库存，不生成应收账款
        sendSaleOutStockMessage(doc, null);
        log.info("自用出库单正式数据已生成: bizId={}, docNo={}", doc.getDocNo(), main.getOutNo());
    }

    private void sendSaleOutFinanceMessage(SaleOutMainDTO outMain, Integer paymentType) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("outNo", outMain.getOutNo());
            data.put("orderNo", outMain.getOrderNo());
            data.put("customerCode", outMain.getCustomerCode());
            data.put("customerName", outMain.getCustomerName());
            data.put("totalAmount", outMain.getTotalAmount());
            data.put("payAmount", outMain.getTotalAmount());
            data.put("payType", paymentType != null ? paymentType : 1);
            data.put("warehouseCode", outMain.getWarehouseCode());

            MqCommonMessage<Map<String, Object>> message = MessageFactory.create(
                    data,
                    RabbitMQConstant.SALE_OUT_EXCHANGE,
                    RabbitMQConstant.SALE_OUT_FINANCE_ROUTING_KEY,
                    "SALE_OUT_FINANCE_CONFIRM"
            );
            mqMessageFacade.sendAsync(message);
            log.info("销售出库财务消息已发送: outNo={}, totalAmount={}", outMain.getOutNo(), outMain.getTotalAmount());
        } catch (Exception e) {
            log.error("发送销售出库财务消息失败: outNo={}, error={}", outMain.getOutNo(), e.getMessage(), e);
        }
    }

    private void sendSaleOutStockMessage(DocFeignResponse doc, String orderNo) {
        try {
            if (doc.getItems() == null || doc.getItems().isEmpty()) {
                return;
            }
            List<Map<String, Object>> itemList = new ArrayList<>();
            for (DocFeignItemResponse docItem : doc.getItems()) {
                Map<String, Object> itemMap = new HashMap<>();
                String skuCode = docItem.getSkuCode() != null ? docItem.getSkuCode() : getDefaultSkuCode(docItem.getGoodsCode());
                BigDecimal conversionRate = getConversionRate(skuCode, docItem.getGoodsUnit(), docItem.getConversionRate());
                BigDecimal stockQuantity = docItem.getQuantity().multiply(conversionRate);
                itemMap.put("skuCode", skuCode);
                itemMap.put("goodsCode", docItem.getGoodsCode());
                itemMap.put("quantity", docItem.getQuantity());
                itemMap.put("stockQuantity", stockQuantity);
                itemMap.put("unitCode", docItem.getGoodsUnit());
                itemMap.put("conversionRate", conversionRate);
                itemMap.put("batchNo", docItem.getBatchNo());
                itemList.add(itemMap);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("outNo", doc.getDocNo());
            data.put("orderNo", orderNo);
            data.put("warehouseCode", doc.getWarehouseCode());
            data.put("action", "CONFIRM");
            data.put("items", itemList);
            MqCommonMessage<Map<String, Object>> message = MessageFactory.create(
                    data,
                    RabbitMQConstant.SALE_OUT_EXCHANGE,
                    RabbitMQConstant.SALE_OUT_STOCK_ROUTING_KEY,
                    "SALE_OUT_STOCK_CONFIRM"
            );
            mqMessageFacade.sendAsync(message);
            log.info("销售出库库存消息已发送: outNo={}, warehouseCode={}, itemCount={}",
                    doc.getDocNo(), doc.getWarehouseCode(), itemList.size());
        } catch (Exception e) {
            log.error("发送销售出库库存消息失败: outNo={}, error={}", doc.getDocNo(), e.getMessage(), e);
        }
    }

    private void saveSaleReturn(DocFeignResponse doc) {
        SaleReturnSaveDTO saveDTO = new SaleReturnSaveDTO();
        saveDTO.setReturnNo(doc.getDocNo());
        saveDTO.setDocName(doc.getDocName());
        saveDTO.setCustomerCode(doc.getPartnerCode());
        saveDTO.setCustomerName(doc.getPartnerName());
        saveDTO.setWarehouseCode(doc.getWarehouseCode());
        saveDTO.setWarehouseName(doc.getWarehouseName());
        saveDTO.setOrderNo(doc.getOrderNo());
        saveDTO.setRemark(doc.getRemark());

        if (doc.getItems() != null && !doc.getItems().isEmpty()) {
            List<SaleReturnItemSaveDTO> items = new ArrayList<>();
            for (DocFeignItemResponse docItem : doc.getItems()) {
                SaleReturnItemSaveDTO item = new SaleReturnItemSaveDTO();
                item.setGoodsId(docItem.getGoodsId());
                item.setGoodsCode(docItem.getGoodsCode());
                String skuCode = docItem.getSkuCode() != null ? docItem.getSkuCode() : getDefaultSkuCode(docItem.getGoodsCode());
                item.setSkuCode(skuCode);
                item.setSkuName(docItem.getSkuName() != null ? docItem.getSkuName() : skuCode);
                item.setGoodsName(docItem.getGoodsName());
                item.setGoodsSpec(docItem.getGoodsSpec());
                item.setUnitCode(docItem.getGoodsUnit());
                item.setConversionRate(docItem.getConversionRate());
                item.setReturnQuantity(docItem.getQuantity());
                item.setUnitPrice(docItem.getUnitPrice());
                item.setTaxRate(docItem.getTaxRate());
                item.setOrderNo(doc.getOrderNo());
                item.setBatchNo(docItem.getBatchNo());
                item.setExpireDate(normalizeExpireDate(docItem.getExpiryDate()));
                items.add(item);
            }
            saveDTO.setItems(items);
        }

        CommonResult<SaleReturnMainDTO> result = saleReturnMainService.save(saveDTO);
        if (result.isSuccess() && result.getData() != null) {
            saleReturnMainService.audit(result.getData().getId(), 1);
        }
        log.info("销售退货单正式数据已生成: bizId={}, docNo={}", doc.getDocNo());
    }
}