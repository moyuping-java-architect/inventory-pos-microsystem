package com.psi.stock.mq.listener;

import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.result.CommonResult;
import com.psi.stock.dto.StockBatchOperateItemDTO;
import com.psi.stock.service.StockMqProcessService;
import com.psi.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 销售订单预占库存MQ监听器
 *
 * <p>监听销售订单库存队列，在销售订单审批通过后预占库存：
 * available_quantity 减少，locked_quantity 增加
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SaleOrderLockListener {

    private final StockService stockService;
    private final StockMqProcessService stockMqProcessService;

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitMQConstant.SALE_ORDER_STOCK_QUEUE)
    public void handleSaleOrderLock(MqCommonMessage<?> message) {
        try {
            Map<String, Object> data = (Map<String, Object>) message.getData();
            String action = data.get("action") != null ? data.get("action").toString() : "LOCK";
            if (!"LOCK".equals(action)) {
                log.debug("销售订单库存消息非预占动作，跳过: action={}", action);
                return;
            }

            String orderNo = (String) data.get("orderNo");
            String warehouseCode = (String) data.get("warehouseCode");
            List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");

            String processKey = orderNo + ":LOCK";
            if (stockMqProcessService.isProcessed(processKey).getData()) {
                log.info("销售订单预占库存消息已处理过，跳过: orderNo={}", orderNo);
                return;
            }

            log.info("收到销售订单预占库存消息，单号: {}, 仓库: {}, 商品数量: {}",
                    orderNo, warehouseCode, items.size());

            List<StockBatchOperateItemDTO> lockItems = new ArrayList<>();
            for (Map<String, Object> item : items) {
                String skuCode = (String) item.get("skuCode");
                BigDecimal stockQuantity = new BigDecimal(item.get("stockQuantity").toString());
                StockBatchOperateItemDTO lockItem = new StockBatchOperateItemDTO();
                lockItem.setWarehouseCode(warehouseCode);
                lockItem.setSkuCode(skuCode);
                lockItem.setQuantity(stockQuantity);
                lockItems.add(lockItem);
            }

            CommonResult<Void> result = stockService.batchLockStock(lockItems, orderNo, "SALE_ORDER");
            if (!result.isSuccess()) {
                log.error("销售订单批量预占库存失败: msg={}", result.getMessage());
                throw new RuntimeException("销售订单预占库存失败: " + result.getMessage());
            }

            stockMqProcessService.markProcessed(processKey);
            log.info("销售订单预占库存处理完成，单号: {}", orderNo);
        } catch (Exception e) {
            log.error("处理销售订单预占库存消息失败", e);
            throw e;
        }
    }
}
