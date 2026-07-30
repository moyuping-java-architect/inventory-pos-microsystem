package com.psi.cashier.controller;

import com.psi.cashier.dto.RefundItemSaveDTO;
import com.psi.cashier.dto.RefundMainSaveDTO;
import com.psi.cashier.dto.RefundPaySaveDTO;
import com.psi.cashier.entity.OrderItemEntity;
import com.psi.cashier.entity.OrderMainEntity;
import com.psi.cashier.entity.OrderPayEntity;
import com.psi.cashier.entity.RefundItemEntity;
import com.psi.cashier.entity.RefundOrderEntity;
import com.psi.cashier.entity.RefundOrderItemEntity;
import com.psi.cashier.entity.RefundPayEntity;
import com.psi.cashier.service.OrderItemService;
import com.psi.cashier.service.OrderMainService;
import com.psi.cashier.service.OrderPayService;
import com.psi.cashier.service.ReceiptPrintService;
import com.psi.cashier.service.RefundItemService;
import com.psi.cashier.service.RefundOrderItemService;
import com.psi.cashier.service.RefundOrderService;
import com.psi.cashier.service.RefundPayService;
import com.psi.cashier.mq.producer.CashierSyncProducer;
import com.psi.common.context.UserContext;
import com.psi.common.context.UserInfo;
import com.psi.common.util.IdUtils;
import com.psi.common.mybatis.util.BatchUtils;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 退货订单控制器
 * 提供退货订单的REST API接口
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/psi/cashier/refund")
public class RefundController {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RefundOrderService refundOrderService;
    private final RefundOrderItemService refundOrderItemService;
    private final RefundPayService refundPayService;
    private final RefundItemService refundItemService;
    private final OrderMainService orderMainService;
    private final OrderItemService orderItemService;
    private final OrderPayService orderPayService;
    private final BatchUtils batchUtils;
    private final CashierSyncProducer cashierSyncProducer;
    private final ReceiptPrintService receiptPrintService;

    public RefundController(RefundOrderService refundOrderService,
                           RefundOrderItemService refundOrderItemService,
                           RefundPayService refundPayService,
                           RefundItemService refundItemService,
                           OrderMainService orderMainService,
                           OrderItemService orderItemService,
                           OrderPayService orderPayService,
                           BatchUtils batchUtils,
                           CashierSyncProducer cashierSyncProducer,
                           ReceiptPrintService receiptPrintService) {
        this.refundOrderService = refundOrderService;
        this.refundOrderItemService = refundOrderItemService;
        this.refundPayService = refundPayService;
        this.refundItemService = refundItemService;
        this.orderMainService = orderMainService;
        this.orderItemService = orderItemService;
        this.orderPayService = orderPayService;
        this.batchUtils = batchUtils;
        this.cashierSyncProducer = cashierSyncProducer;
        this.receiptPrintService = receiptPrintService;
    }

    @GetMapping("/page")
    public PageResult<RefundOrderEntity> queryPage(@RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "10") int pageSize,
                                                   @RequestParam(required = false) String refundNo,
                                                   @RequestParam(required = false) Integer operatorId) {
        return refundOrderService.queryPage(pageNum, pageSize, refundNo, operatorId);
    }

    @GetMapping("/{refundNo}")
    public CommonResult<RefundOrderEntity> getByRefundNo(@PathVariable String refundNo) {
        RefundOrderEntity refund = refundOrderService.getByRefundNo(refundNo);
        if (refund == null) {
            return CommonResult.fail("退货单不存在");
        }
        return CommonResult.success(refund);
    }

    @GetMapping("/{refundNo}/items")
    public CommonResult<List<RefundOrderItemEntity>> getRefundItems(@PathVariable String refundNo) {
        List<RefundOrderItemEntity> items = refundOrderItemService.getByRefundNo(refundNo);
        return CommonResult.success(items);
    }

    @GetMapping("/{refundNo}/pays")
    public CommonResult<List<RefundPayEntity>> getRefundPays(@PathVariable String refundNo) {
        List<RefundPayEntity> pays = refundPayService.getByRefundNo(refundNo);
        return CommonResult.success(pays);
    }

    @GetMapping("/source/{sourceOrderNo}")
    public CommonResult<List<RefundOrderEntity>> getBySourceOrderNo(@PathVariable String sourceOrderNo) {
        List<RefundOrderEntity> refunds = refundOrderService.getBySourceOrderNo(sourceOrderNo);
        return CommonResult.success(refunds);
    }

    @PostMapping
    public CommonResult<RefundOrderEntity> create(@RequestBody Map<String, Object> requestBody) {
        String sourceOrderNo = (String) requestBody.get("sourceOrderNo");
        String refundReason = (String) requestBody.get("refundReason");
        
        if (sourceOrderNo == null || sourceOrderNo.isEmpty()) {
            return CommonResult.fail("原订单号不能为空");
        }
        
        OrderMainEntity sourceOrder = orderMainService.getByOrderNo(sourceOrderNo);
        if (sourceOrder == null) {
            return CommonResult.fail("原订单不存在");
        }
        
        if (refundOrderService.isOrderFullyRefunded(sourceOrderNo)) {
            return CommonResult.fail("该订单已整单退货，无法再次退货");
        }
        
        List<OrderItemEntity> sourceItems = orderItemService.getByOrderNo(sourceOrderNo);
        
        List<RefundItemSaveDTO> refundItems = new ArrayList<>();
        BigDecimal totalRefund = BigDecimal.ZERO;
        
        List<Map<String, Object>> items = (List<Map<String, Object>>) requestBody.get("items");
        if (items == null || items.isEmpty()) {
            return CommonResult.fail("请选择退货商品");
        }
        
        for (Map<String, Object> item : items) {
            Integer index = ((Number) item.get("index")).intValue();
            Integer quantity = ((Number) item.get("quantity")).intValue();
            
            if (index >= 0 && index < sourceItems.size() && quantity > 0) {
                OrderItemEntity sourceItem = sourceItems.get(index);
                
                RefundItemSaveDTO refundItem = new RefundItemSaveDTO();
                refundItem.setSkuId(sourceItem.getSkuId() != null ? String.valueOf(sourceItem.getSkuId()) : null);
                refundItem.setSkuCode(sourceItem.getSkuCode());
                refundItem.setBarCode(sourceItem.getBarCode());
                refundItem.setProductName(sourceItem.getProductName());
                refundItem.setSaleUnitName(sourceItem.getSaleUnitName());
                refundItem.setRefundQuantity(BigDecimal.valueOf(quantity));
                refundItem.setRefundPrice(sourceItem.getUnitPrice());
                refundItem.setTaxRate(sourceItem.getTaxRate());
                refundItem.setIsTaxInclusive(sourceItem.getIsTaxInclusive());
                refundItem.setBatchNo(sourceItem.getBatchNo());
                refundItem.setCurrency(sourceItem.getCurrency());

                BigDecimal subtotal = sourceItem.getUnitPrice().multiply(BigDecimal.valueOf(quantity));
                BigDecimal taxRate = sourceItem.getTaxRate() != null ? sourceItem.getTaxRate() : BigDecimal.ZERO;
                boolean taxInclusive = sourceItem.getIsTaxInclusive() != null && sourceItem.getIsTaxInclusive() == 1;
                BigDecimal netAmount;
                BigDecimal taxAmount;
                if (taxInclusive) {
                    netAmount = subtotal.divide(BigDecimal.ONE.add(taxRate), 4, RoundingMode.HALF_UP);
                    taxAmount = netAmount.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
                    netAmount = subtotal.subtract(taxAmount);
                } else {
                    netAmount = subtotal;
                    taxAmount = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
                }
                refundItem.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
                refundItem.setNetAmount(netAmount.setScale(2, RoundingMode.HALF_UP));
                refundItem.setTaxAmount(taxAmount);
                totalRefund = totalRefund.add(refundItem.getSubtotal());

                refundItems.add(refundItem);
            }
        }
        
        if (refundItems.isEmpty()) {
            return CommonResult.fail("请选择有效的退货商品");
        }
        
        String checkResult = refundOrderService.checkItemRefundQuantity(sourceOrderNo, refundItems);
        if (checkResult != null) {
            return CommonResult.fail(checkResult);
        }
        
        List<OrderPayEntity> sourcePays = orderPayService.getByOrderNo(sourceOrderNo);
        
        BigDecimal netRefund = refundItems.stream()
                .map(RefundItemSaveDTO::getNetAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxRefund = refundItems.stream()
                .map(RefundItemSaveDTO::getTaxAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        RefundMainSaveDTO refundDTO = new RefundMainSaveDTO();
        refundDTO.setSourceOrderNo(sourceOrderNo);
        refundDTO.setShopCode(sourceOrder.getShopCode());
        refundDTO.setPosId(sourceOrder.getPosId());
        refundDTO.setOperatorId(sourceOrder.getOperatorId());
        refundDTO.setRemark(refundReason);
        refundDTO.setItems(refundItems);
        refundDTO.setTotalRefund(totalRefund);
        refundDTO.setNetRefund(netRefund);
        refundDTO.setTaxRefund(taxRefund);
        refundDTO.setCurrency(sourceOrder.getCurrency());
        refundDTO.setExchangeRate(sourceOrder.getExchangeRate());
        refundDTO.setOriginalRefund(sourceOrder.getOriginalAmount());

        RefundOrderEntity refund = new RefundOrderEntity();
        refund.setRefundNo("RFD" + System.currentTimeMillis());
        refund.setSourceOrderNo(sourceOrderNo);
        refund.setShopCode(sourceOrder.getShopCode());
        refund.setPosId(sourceOrder.getPosId());
        refund.setBizType(3);
        refund.setOperatorId(sourceOrder.getOperatorId());
        refund.setTotalRefund(totalRefund);
        refund.setNetRefund(netRefund);
        refund.setTaxRefund(taxRefund);
        refund.setCurrency(sourceOrder.getCurrency());
        refund.setExchangeRate(sourceOrder.getExchangeRate());
        refund.setOriginalRefund(sourceOrder.getOriginalAmount());
        refund.setRemark(refundReason);
        
        // 设置租户ID
        refund.setTenantId(UserContext.getTenantId());
        
        // 设置创建时间、更新时间和退货时间
        String currentTime = LocalDateTime.now().format(DATETIME_FORMATTER);
        refund.setCreateTime(currentTime);
        refund.setUpdateTime(currentTime);
        refund.setRefundTime(currentTime);
        
        // 设置创建人和更新人
        UserInfo userInfo = UserContext.get();
        if (userInfo != null) {
            refund.setCreateBy(userInfo.getUpdateUserId());
            refund.setUpdateBy(userInfo.getUpdateUserId());
        } else {
            refund.setCreateBy(String.valueOf(sourceOrder.getOperatorId()));
            refund.setUpdateBy(String.valueOf(sourceOrder.getOperatorId()));
        }
        
        RefundOrderEntity saved = refundOrderService.save(refund);
        
        List<RefundOrderItemEntity> itemEntityList = new ArrayList<>();
        for (RefundItemSaveDTO item : refundItems) {
            RefundOrderItemEntity itemEntity = new RefundOrderItemEntity();
            itemEntity.setDataUuid(IdUtils.snowflakeIdStr());
            itemEntity.setTenantId(UserContext.getTenantId());
            itemEntity.setShopCode(sourceOrder.getShopCode());
            itemEntity.setPosId(sourceOrder.getPosId());
            itemEntity.setRefundNo(saved.getRefundNo());
            itemEntity.setSkuId(parseIntegerSafely(item.getSkuId()));
            itemEntity.setSkuCode(item.getSkuCode());
            itemEntity.setBarCode(item.getBarCode());
            itemEntity.setProductName(item.getProductName());
            itemEntity.setSaleUnitName(item.getSaleUnitName());
            itemEntity.setRefundQuantity(item.getRefundQuantity());
            itemEntity.setRefundPrice(item.getRefundPrice());
            itemEntity.setSubtotal(item.getSubtotal());
            itemEntity.setTaxRate(item.getTaxRate());
            itemEntity.setIsTaxInclusive(item.getIsTaxInclusive());
            itemEntity.setNetAmount(item.getNetAmount());
            itemEntity.setTaxAmount(item.getTaxAmount());
            itemEntity.setBatchNo(item.getBatchNo());
            itemEntity.setCurrency(item.getCurrency());
            itemEntityList.add(itemEntity);
        }
        batchUtils.saveBatch(refundOrderItemService, itemEntityList);
        
        List<Map<String, Object>> payDetails = (List<Map<String, Object>>) requestBody.get("payDetails");
        if (payDetails == null || payDetails.isEmpty()) {
            return CommonResult.fail("请添加退款方式和金额");
        }
        
        List<RefundPayEntity> refundPayList = new ArrayList<>();
        String payName = "";
        for (Map<String, Object> payDetail : payDetails) {
            Integer payType = ((Number) payDetail.get("payType")).intValue();
            Double refundAmount = ((Number) payDetail.get("refundAmount")).doubleValue();
            
            switch (payType) {
                case 1: payName = "现金"; break;
                case 2: payName = "微信"; break;
                case 3: payName = "支付宝"; break;
                case 4: payName = "会员卡"; break;
                default: payName = "其他";
            }
            
            RefundPayEntity refundPay = new RefundPayEntity();
            refundPay.setDataUuid(IdUtils.snowflakeIdStr());
            refundPay.setTenantId(UserContext.getTenantId());
            refundPay.setShopCode(sourceOrder.getShopCode());
            refundPay.setPosId(sourceOrder.getPosId());
            refundPay.setRefundNo(saved.getRefundNo());
            refundPay.setBizType(7);
            refundPay.setPayId(payType);
            refundPay.setPayName(payName);
            refundPay.setRefundAmount(BigDecimal.valueOf(refundAmount));
            refundPay.setCurrency(sourceOrder.getCurrency());
            refundPay.setRefundTime(currentTime);
            refundPay.setCreateBy(String.valueOf(sourceOrder.getOperatorId()));
            refundPay.setCreateTime(currentTime);
            refundPay.setUpdateBy(String.valueOf(sourceOrder.getOperatorId()));
            refundPay.setUpdateTime(currentTime);
            refundPayList.add(refundPay);
        }
        batchUtils.saveBatch(refundPayService, refundPayList);
        
        // 异步发送退款同步消息到sync-ms
        cashierSyncProducer.syncRefundAsync(saved.getRefundNo());
        log.info("已触发退款同步消息，退款单号：{}", saved.getRefundNo());
        
        // 补充支付信息到已有的退款DTO（用于打印）
        if (!refundPayList.isEmpty()) {
            List<RefundPaySaveDTO> payDTOs = new ArrayList<>();
            for (RefundPayEntity pay : refundPayList) {
                RefundPaySaveDTO payDTO = new RefundPaySaveDTO();
                payDTO.setPayId(pay.getPayId());
                payDTO.setPayName(pay.getPayName());
                payDTO.setRefundAmount(pay.getRefundAmount());
                payDTO.setCurrency(pay.getCurrency());
                payDTOs.add(payDTO);
            }
            refundDTO.setPays(payDTOs);
        }
        
        // 异步打印退款小票（直接使用DTO数据，避免重复查询数据库）
        try {
            receiptPrintService.printRefund(refundDTO, saved.getRefundNo());
            log.info("已触发异步打印退款小票（直接使用DTO数据），退款单号：{}", saved.getRefundNo());
        } catch (Exception e) {
            log.warn("触发退款小票打印失败，退款单号：{}，错误：{}", saved.getRefundNo(), e.getMessage());
        }
        
        return CommonResult.success(saved);
    }

    @PutMapping("/{refundNo}")
    public CommonResult<RefundOrderEntity> update(@PathVariable String refundNo, @RequestBody RefundOrderEntity refund) {
        RefundOrderEntity existing = refundOrderService.getByRefundNo(refundNo);
        if (existing == null) {
            return CommonResult.fail("退货单不存在");
        }
        refund.setId(existing.getId());
        refund.setRefundNo(refundNo);
        refundOrderService.update(refund);
        return CommonResult.success(refund);
    }

    @DeleteMapping("/{refundNo}")
    public CommonResult<Void> delete(@PathVariable String refundNo) {
        boolean deleted = refundOrderService.deleteByRefundNo(refundNo);
        if (!deleted) {
            return CommonResult.fail("退货单不存在");
        }
        return CommonResult.success(null);
    }

    /**
     * 安全解析字符串为 Integer
     * 如果字符串为 null、空或无法解析为整数，则返回 null
     *
     * @param value 待解析的字符串
     * @return 解析后的 Integer，解析失败返回 null
     */
    private Integer parseIntegerSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("无法解析字符串为整数: {}", value);
            return null;
        }
    }
}