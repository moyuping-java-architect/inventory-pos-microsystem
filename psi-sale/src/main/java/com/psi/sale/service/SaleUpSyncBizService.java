package com.psi.sale.service;

import com.psi.common.async.facade.MqMessageFacade;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.message.MessageFactory;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.result.CommonResult;
import com.psi.common.util.IdUtils;
import com.psi.sale.dto.SaleOutItemSaveDTO;
import com.psi.sale.dto.SaleOutMainDTO;
import com.psi.sale.dto.SaleOutSaveDTO;
import com.psi.sale.entity.SaleOrderItemEntity;
import com.psi.sale.entity.SaleOrderMainEntity;
import com.psi.sale.entity.SaleReturnItemEntity;
import com.psi.sale.entity.SaleReturnMainEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleUpSyncBizService {

    private final SaleOrderMainService saleOrderMainService;
    private final SaleOrderItemService saleOrderItemService;
    private final SaleOutMainService saleOutMainService;
    private final SaleOutItemService saleOutItemService;
    private final SaleReturnMainService saleReturnMainService;
    private final SaleReturnItemService saleReturnItemService;
    private final MqMessageFacade mqMessageFacade;

    @Transactional(rollbackFor = Exception.class)
    public void batchProcessSaleOrders(List<SaleOrderGroup> groups) {
        log.info("[sale] 批量处理销售订单后置业务: count={}", groups.size());
        for (SaleOrderGroup group : groups) {
            try {
                processSingleSaleOrder(group);
            } catch (Exception e) {
                log.error("[sale] 处理单个销售订单失败: orderNo={}, error={}",
                        group.orderNo(), e.getMessage(), e);
                throw e;
            }
        }
    }

    private void processSingleSaleOrder(SaleOrderGroup group) {
        SaleOrderMainEntity order = group.main();
        List<SaleOrderItemEntity> items = group.items();

        // 保存销售订单
        order.setOrderStatus(1);
        saleOrderMainService.save(order);
        for (SaleOrderItemEntity item : items) {
            item.setOrderId(order.getId());
        }
        saleOrderItemService.saveBatch(items);
        log.info("[sale] 销售订单已保存: orderNo={}, id={}", order.getOrderNo(), order.getId());

        // 预占库存
        saleOrderMainService.audit(order.getId(), 1);
        log.info("[sale] 销售订单预占库存完成: orderNo={}", order.getOrderNo());

        // 生成销售出库单
        SaleOutSaveDTO outDTO = buildSaleOutSaveDTO(order, items);
        CommonResult<SaleOutMainDTO> outResult = saleOutMainService.save(outDTO);
        if (!outResult.isSuccess() || outResult.getData() == null) {
            throw new RuntimeException("生成销售出库单失败: orderNo=" + order.getOrderNo() + ", msg=" + outResult.getMessage());
        }
        Long outId = outResult.getData().getId();
        String outNo = outResult.getData().getOutNo();
        log.info("[sale] 销售出库单已生成: orderNo={}, outNo={}", order.getOrderNo(), outNo);

        // 扣减库存并释放预占
        saleOutMainService.audit(outId, 2);
        log.info("[sale] 销售出库单库存扣减完成: outNo={}", outNo);

        // 发送销售出库财务 MQ
        sendSaleOutFinanceMessage(outResult.getData(), order);
    }

    private SaleOutSaveDTO buildSaleOutSaveDTO(SaleOrderMainEntity order, List<SaleOrderItemEntity> items) {
        SaleOutSaveDTO dto = new SaleOutSaveDTO();
        dto.setDocName(order.getDocName());
        dto.setOutNo("SOUT" + IdUtils.generateId());
        dto.setOrderNo(order.getOrderNo());
        dto.setCustomerId(order.getCustomerId());
        dto.setCustomerCode(order.getCustomerCode());
        dto.setCustomerName(order.getCustomerName());
        dto.setOutDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        dto.setWarehouseCode(order.getWarehouseCode());
        dto.setWarehouseName(order.getWarehouseName());
        dto.setRemark("由POS销售订单同步生成: " + order.getOrderNo());

        List<SaleOutItemSaveDTO> outItems = new ArrayList<>();
        for (SaleOrderItemEntity item : items) {
            SaleOutItemSaveDTO outItem = new SaleOutItemSaveDTO();
            outItem.setOrderId(order.getId());
            outItem.setOrderNo(order.getOrderNo());
            outItem.setItemNo(item.getItemNo());
            outItem.setGoodsId(item.getGoodsId());
            outItem.setGoodsCode(item.getGoodsCode());
            outItem.setSkuCode(item.getSkuCode());
            outItem.setSkuName(item.getSkuName());
            outItem.setGoodsName(item.getGoodsName());
            outItem.setGoodsSpec(item.getGoodsSpec());
            outItem.setUnitCode(item.getUnitCode());
            outItem.setConversionRate(item.getConversionRate());
            outItem.setOrderQuantity(item.getQuantity());
            outItem.setOutQuantity(item.getQuantity());
            outItem.setUnitPrice(item.getUnitPrice());
            outItem.setTaxRate(item.getTaxRate());
            outItem.setBatchNo(null);
            outItem.setExpireDate(null);
            outItem.setRemark(item.getRemark());
            outItems.add(outItem);
        }
        dto.setItems(outItems);
        return dto;
    }

    private void sendSaleOutFinanceMessage(SaleOutMainDTO outDTO, SaleOrderMainEntity order) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("outNo", outDTO.getOutNo());
            data.put("orderNo", order.getOrderNo());
            data.put("customerCode", order.getCustomerCode());
            data.put("customerName", order.getCustomerName());
            data.put("totalAmount", outDTO.getTotalAmount());
            data.put("payAmount", order.getPayAmount());
            data.put("payType", order.getPaymentType());
            data.put("warehouseCode", order.getWarehouseCode());

            MqCommonMessage<Map<String, Object>> message = MessageFactory.create(
                    data,
                    RabbitMQConstant.SALE_OUT_EXCHANGE,
                    RabbitMQConstant.SALE_OUT_FINANCE_ROUTING_KEY,
                    "SALE_OUT_FINANCE"
            );
            mqMessageFacade.send(message);
            log.info("[sale] 销售出库财务消息已发送: outNo={}", outDTO.getOutNo());
        } catch (Exception e) {
            log.error("[sale] 发送销售出库财务消息失败: outNo={}, error={}", outDTO.getOutNo(), e.getMessage(), e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchProcessSaleReturns(List<SaleReturnGroup> groups) {
        log.info("[sale] 批量处理销售退货后置业务: count={}", groups.size());
        for (SaleReturnGroup group : groups) {
            try {
                processSingleSaleReturn(group);
            } catch (Exception e) {
                log.error("[sale] 处理单个销售退货失败: returnNo={}, error={}",
                        group.returnNo(), e.getMessage(), e);
                throw e;
            }
        }
    }

    private void processSingleSaleReturn(SaleReturnGroup group) {
        SaleReturnMainEntity returnEntity = group.main();
        List<SaleReturnItemEntity> items = group.items();

        // 保存销售退货单
        returnEntity.setOrderStatus(1);
        saleReturnMainService.save(returnEntity);
        for (SaleReturnItemEntity item : items) {
            item.setReturnId(returnEntity.getId());
        }
        saleReturnItemService.saveBatch(items);
        log.info("[sale] 销售退货单已保存: returnNo={}, id={}", returnEntity.getReturnNo(), returnEntity.getId());

        // 增加库存
        saleReturnMainService.audit(returnEntity.getId(), 1);
        log.info("[sale] 销售退货库存增加完成: returnNo={}", returnEntity.getReturnNo());

        // 发送销售退货财务 MQ
        sendSaleReturnFinanceMessage(returnEntity, items);
    }

    private void sendSaleReturnFinanceMessage(SaleReturnMainEntity returnEntity, List<SaleReturnItemEntity> items) {
        try {
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (SaleReturnItemEntity item : items) {
                if (item.getReturnQuantity() != null && item.getUnitPrice() != null) {
                    totalAmount = totalAmount.add(item.getReturnQuantity().multiply(item.getUnitPrice()));
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("returnNo", returnEntity.getReturnNo());
            data.put("orderNo", returnEntity.getOrderNo());
            data.put("customerCode", returnEntity.getCustomerCode());
            data.put("customerName", returnEntity.getCustomerName());
            data.put("totalAmount", totalAmount);
            data.put("warehouseCode", returnEntity.getWarehouseCode());

            MqCommonMessage<Map<String, Object>> message = MessageFactory.create(
                    data,
                    RabbitMQConstant.SALE_RETURN_EXCHANGE,
                    RabbitMQConstant.SALE_RETURN_FINANCE_ROUTING_KEY,
                    "SALE_RETURN_FINANCE"
            );
            mqMessageFacade.send(message);
            log.info("[sale] 销售退货财务消息已发送: returnNo={}", returnEntity.getReturnNo());
        } catch (Exception e) {
            log.error("[sale] 发送销售退货财务消息失败: returnNo={}, error={}", returnEntity.getReturnNo(), e.getMessage(), e);
        }
    }

    public record SaleOrderGroup(String orderNo, SaleOrderMainEntity main, List<SaleOrderItemEntity> items) {
    }

    public record SaleReturnGroup(String returnNo, SaleReturnMainEntity main, List<SaleReturnItemEntity> items) {
    }
}
