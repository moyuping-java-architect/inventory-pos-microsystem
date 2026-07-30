package com.psi.purchase.mq.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.feign.DocFeignClient;
import com.psi.common.feign.DocFeignResponse;
import com.psi.common.feign.DocFeignResponse.DocFeignItemResponse;
import com.psi.common.idempotent.MessageIdempotencyService;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.result.CommonResult;
import com.psi.purchase.dto.PurchaseInItemSaveDTO;
import com.psi.purchase.dto.PurchaseInMainDTO;
import com.psi.purchase.dto.PurchaseInSaveDTO;
import com.psi.purchase.dto.PurchaseOrderItemSaveDTO;
import com.psi.purchase.dto.PurchaseOrderMainDTO;
import com.psi.purchase.dto.PurchaseOrderSaveDTO;
import com.psi.purchase.dto.PurchaseReturnItemSaveDTO;
import com.psi.purchase.dto.PurchaseReturnMainDTO;
import com.psi.purchase.dto.PurchaseReturnSaveDTO;
import com.psi.purchase.service.PurchaseInMainService;
import com.psi.purchase.service.PurchaseOrderMainService;
import com.psi.purchase.service.PurchaseReturnMainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 采购流程完成MQ监听器
 * 
 * <p>监听采购流程完成队列，当采购订单/采购入库/采购退货审批通过后，
 * 生成采购模块正式业务数据
 * 
 * <p>通过 Feign 远程调用 psi-flow 获取单据数据并更新状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PurchaseProcessCompletedListener {

    private final DocFeignClient docFeignClient;
    private final PurchaseOrderMainService purchaseOrderMainService;
    private final PurchaseInMainService purchaseInMainService;
    private final PurchaseReturnMainService purchaseReturnMainService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final MessageIdempotencyService messageIdempotencyService;

    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4})[-/](\\d{1,2})[-/](\\d{1,2})");

    /**
     * 规范化有效期日期格式为 yyyy-MM-dd
     * 处理各种前端传入的格式，如：2026-06-25T00:00:00.000Z、2026/06/25 15:30:00、2026-06-25等
     * 避免超长日期字符串导致数据库写入失败（purchase_in_item.expire_date 是 VARCHAR(20)）
     */
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

    private String normalizeExpireDate(String expiryDate) {
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            return null;
        }
        try {
            Matcher matcher = DATE_PATTERN.matcher(expiryDate);
            if (matcher.find()) {
                int year = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                int day = Integer.parseInt(matcher.group(3));
                return String.format("%04d-%02d-%02d", year, month, day);
            }
            log.warn("无法识别的有效期格式，直接截断前10个字符: {}", expiryDate);
            return expiryDate.length() > 10 ? expiryDate.substring(0, 10) : expiryDate;
        } catch (Exception e) {
            log.warn("有效期解析异常，直接截断: {}, error: {}", expiryDate, e.getMessage());
            return expiryDate.length() > 10 ? expiryDate.substring(0, 10) : expiryDate;
        }
    }

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitMQConstant.PROCESS_COMPLETED_PURCHASE_QUEUE)
    public void onProcessCompleted(MqCommonMessage<?> message) {
        Map<String, Object> data = (Map<String, Object>) message.getData();
        String bizId = (String) data.get("bizId");
        String messageId = message.getMessageId();
        log.info("收到采购流程完成消息: bizId={}, messageId={}", bizId, messageId);

        messageIdempotencyService.execute(messageId, () -> {
            try {
                DocFeignResponse doc = resolveDocFromMessage(data, bizId);
                if (doc == null) {
                    return null;
                }

                if ("PURCHASE_RETURN".equals(doc.getDocType())) {
                    savePurchaseReturn(doc);
                } else if ("PURCHASE_IN".equals(doc.getDocType())) {
                    savePurchaseIn(doc);
                } else {
                    savePurchaseOrder(doc);
                }

                // 单据状态已在 FlowEngineServiceImpl.updateDocStatus() 中更新，此处仅生成正式数据
                log.info("采购单据正式数据已生成: bizId={}, docType={}", bizId, doc.getDocType());
            } catch (Exception e) {
                log.error("处理采购流程完成消息失败: bizId={}, error={}", bizId, e.getMessage(), e);
                throw e;
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
                log.info("采购流程完成：从MQ消息中解析单据数据: bizId={}", bizId);
                return doc;
            } catch (Exception e) {
                log.warn("采购流程完成：MQ中docData解析失败，降级Feign查询: bizId={}, error={}", bizId, e.getMessage());
            }
        }

        CommonResult<DocFeignResponse> result = docFeignClient.findByDocNo(bizId);
        if (!result.isSuccess() || result.getData() == null) {
            log.warn("采购流程完成：单据不存在或查询失败: bizId={}, msg={}", bizId, result.getMessage());
            return null;
        }
        log.info("采购流程完成：通过Feign查询单据数据: bizId={}", bizId);
        return result.getData();
    }

    private void savePurchaseOrder(DocFeignResponse doc) {
        PurchaseOrderSaveDTO saveDTO = new PurchaseOrderSaveDTO();
        saveDTO.setOrderNo(doc.getDocNo());
        saveDTO.setDocName(doc.getDocName());
        saveDTO.setSupplierCode(doc.getPartnerCode());
        saveDTO.setSupplierName(doc.getPartnerName());
        saveDTO.setPaymentType(doc.getPaymentType());
        saveDTO.setCurrencyCode(doc.getCurrencyCode());
        saveDTO.setExchangeRate(doc.getExchangeRate());
        saveDTO.setRemark(doc.getRemark());
        saveDTO.setOrderDate(doc.getDocDate());
        saveDTO.setDeliveryDate(doc.getDeliveryDate());

        if (doc.getItems() != null && !doc.getItems().isEmpty()) {
            List<PurchaseOrderItemSaveDTO> items = new ArrayList<>();
            for (DocFeignItemResponse docItem : doc.getItems()) {
                PurchaseOrderItemSaveDTO item = new PurchaseOrderItemSaveDTO();
                item.setGoodsId(docItem.getGoodsId());
                item.setGoodsCode(docItem.getGoodsCode());
                String skuCode = docItem.getSkuCode() != null ? docItem.getSkuCode() : getDefaultSkuCode(docItem.getGoodsCode());
                item.setSkuCode(skuCode);
                item.setSkuName(docItem.getSkuName() != null ? docItem.getSkuName() : skuCode);
                item.setGoodsName(docItem.getGoodsName());
                item.setGoodsSpec(docItem.getGoodsSpec());
                item.setUnitCode(docItem.getGoodsUnit());
                item.setQuantity(docItem.getQuantity());
                item.setUnitPrice(docItem.getUnitPrice());
                item.setTaxRate(docItem.getTaxRate());
                item.setDiscountRate(docItem.getDiscountRate());
                items.add(item);
            }
            saveDTO.setItems(items);
        }

        CommonResult<PurchaseOrderMainDTO> result = purchaseOrderMainService.save(saveDTO);
        if (result.isSuccess() && result.getData() != null) {
            purchaseOrderMainService.audit(result.getData().getId(), 1);
        }
        log.info("采购订单正式数据已生成: bizId={}, docNo={}", doc.getDocNo(), doc.getDocType());
    }

    private void savePurchaseReturn(DocFeignResponse doc) {
        PurchaseReturnSaveDTO saveDTO = new PurchaseReturnSaveDTO();
        saveDTO.setReturnNo(doc.getDocNo());
        saveDTO.setDocName(doc.getDocName());
        saveDTO.setSupplierCode(doc.getPartnerCode());
        saveDTO.setSupplierName(doc.getPartnerName());
        saveDTO.setWarehouseCode(doc.getWarehouseCode());
        saveDTO.setWarehouseName(doc.getWarehouseName());
        saveDTO.setReturnDate(doc.getDocDate());
        saveDTO.setOrderNo(doc.getOrderNo());
        saveDTO.setRemark(doc.getRemark());
        if (doc.getRemark() != null && !doc.getRemark().isEmpty()) {
            saveDTO.setReturnReason(doc.getRemark());
        } else if (doc.getItems() != null) {
            String firstReason = doc.getItems().stream()
                    .filter(it -> it.getRemark() != null && !it.getRemark().isEmpty())
                    .map(DocFeignItemResponse::getRemark)
                    .findFirst()
                    .orElse(null);
            saveDTO.setReturnReason(firstReason);
        }

        if (doc.getItems() != null && !doc.getItems().isEmpty()) {
            List<PurchaseReturnItemSaveDTO> items = new ArrayList<>();
            for (DocFeignItemResponse docItem : doc.getItems()) {
                PurchaseReturnItemSaveDTO item = new PurchaseReturnItemSaveDTO();
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
                item.setRemark(docItem.getRemark());
                items.add(item);
            }
            saveDTO.setItems(items);
        }

        CommonResult<PurchaseReturnMainDTO> result = purchaseReturnMainService.save(saveDTO);
        if (result.isSuccess() && result.getData() != null) {
            purchaseReturnMainService.audit(result.getData().getId(), 1);
        }
        log.info("采购退货单正式数据已生成: bizId={}, docNo={}", doc.getDocNo(), doc.getDocType());
    }

    private void savePurchaseIn(DocFeignResponse doc) {
        PurchaseInSaveDTO saveDTO = new PurchaseInSaveDTO();
        saveDTO.setInNo(doc.getDocNo());
        saveDTO.setDocName(doc.getDocName());
        saveDTO.setSupplierCode(doc.getPartnerCode());
        saveDTO.setSupplierName(doc.getPartnerName());
        saveDTO.setWarehouseCode(doc.getWarehouseCode());
        saveDTO.setWarehouseName(doc.getWarehouseName());
        saveDTO.setOrderNo(doc.getOrderNo());
        saveDTO.setInDate(doc.getDocDate());
        saveDTO.setRemark(doc.getRemark());

        if (doc.getItems() != null && !doc.getItems().isEmpty()) {
            List<PurchaseInItemSaveDTO> items = new ArrayList<>();
            for (DocFeignItemResponse docItem : doc.getItems()) {
                PurchaseInItemSaveDTO item = new PurchaseInItemSaveDTO();
                item.setGoodsId(docItem.getGoodsId());
                item.setGoodsCode(docItem.getGoodsCode());
                String skuCode = docItem.getSkuCode() != null ? docItem.getSkuCode() : getDefaultSkuCode(docItem.getGoodsCode());
                item.setSkuCode(skuCode);
                item.setSkuName(docItem.getSkuName() != null ? docItem.getSkuName() : skuCode);
                item.setGoodsName(docItem.getGoodsName());
                item.setGoodsSpec(docItem.getGoodsSpec());
                item.setUnitCode(docItem.getGoodsUnit());
                item.setConversionRate(docItem.getConversionRate());
                item.setInQuantity(docItem.getQuantity());
                item.setUnitPrice(docItem.getUnitPrice());
                item.setTaxRate(docItem.getTaxRate());
                item.setBatchNo(docItem.getBatchNo());
                item.setExpireDate(normalizeExpireDate(docItem.getExpiryDate()));
                item.setRemark(docItem.getRemark());
                items.add(item);
            }
            saveDTO.setItems(items);
        }

        CommonResult<PurchaseInMainDTO> result = purchaseInMainService.save(saveDTO);
        if (result.isSuccess() && result.getData() != null) {
            purchaseInMainService.audit(result.getData().getId(), 1);
        }
        log.info("采购入库单正式数据已生成: bizId={}, docNo={}", doc.getDocNo(), doc.getDocType());
    }
}