package com.psi.stock.mq.listener;

import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.result.CommonResult;
import com.psi.stock.dto.StockBatchOperateItemDTO;
import com.psi.stock.service.StockService;
import com.psi.stock.service.StockFlowService;
import com.psi.stock.service.StockMqProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 销售出库库存MQ监听器
 *
 * <p>监听销售出库消息，执行库存扣减操作：
 * <ul>
 *   <li>关联销售订单（orderNo 不为空）：扣减预占库存，quantity 与 locked_quantity 减少</li>
 *   <li>自用单/直接出库（orderNo 为空）：直接扣减可用库存，quantity 与 available_quantity 减少</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SaleOutStockListener {

    private final StockService stockService;
    private final StockFlowService stockFlowService;
    private final StockMqProcessService stockMqProcessService;

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitMQConstant.SALE_OUT_STOCK_QUEUE)
    public void handleSaleOut(MqCommonMessage<?> message) {
        try {
            Map<String, Object> data = (Map<String, Object>) message.getData();
            String outNo = (String) data.get("outNo");
            String orderNo = (String) data.get("orderNo");
            String warehouseCode = (String) data.get("warehouseCode");
            List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");

            // 幂等性检查
            if (stockMqProcessService.isProcessed(outNo).getData()) {
                log.info("销售出库库存消息已处理过，跳过: outNo={}", outNo);
                return;
            }

            boolean selfUse = orderNo == null || orderNo.trim().isEmpty();
            log.info("收到销售出库库存消息，单号: {}, 仓库: {}, 商品数量: {}, 类型: {}",
                    outNo, warehouseCode, items.size(), selfUse ? "自用单" : "关联订单");

            List<StockBatchOperateItemDTO> operateItems = new ArrayList<>();
            for (Map<String, Object> item : items) {
                String skuCode = (String) item.get("skuCode");
                BigDecimal stockQuantity = new BigDecimal(item.get("stockQuantity").toString());
                StockBatchOperateItemDTO operateItem = new StockBatchOperateItemDTO();
                operateItem.setWarehouseCode(warehouseCode);
                operateItem.setSkuCode(skuCode);
                operateItem.setQuantity(stockQuantity);
                operateItems.add(operateItem);
            }

            CommonResult<Void> result;
            if (selfUse) {
                result = stockService.batchDecreaseStock(operateItems, outNo, "SALE_OUT_SELF_USE");
            } else {
                result = stockService.batchConfirmStock(operateItems, outNo, "SALE_OUT");
            }
            if (!result.isSuccess()) {
                log.error("销售出库库存扣减失败: msg={}", result.getMessage());
                throw new RuntimeException("销售出库库存扣减失败: " + result.getMessage());
            }

            // 标记处理完成
            stockMqProcessService.markProcessed(outNo);
            log.info("销售出库库存处理完成，单号: {}", outNo);
        } catch (Exception e) {
            log.error("处理销售出库库存消息失败", e);
            throw e;
        }
    }
}
