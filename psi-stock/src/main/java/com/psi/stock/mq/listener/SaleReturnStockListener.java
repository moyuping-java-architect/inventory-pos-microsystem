package com.psi.stock.mq.listener;

import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.result.CommonResult;
import com.psi.stock.service.StockService;
import com.psi.stock.service.StockMqProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 销售退货库存监听器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SaleReturnStockListener {

    private final StockService stockService;
    private final StockMqProcessService stockMqProcessService;

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitMQConstant.SALE_RETURN_STOCK_QUEUE)
    public void handleSaleReturn(MqCommonMessage<?> message) {
        try {
            Map<String, Object> data = (Map<String, Object>) message.getData();
            String returnNo = (String) data.get("returnNo");
            String warehouseCode = (String) data.get("warehouseCode");
            List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");

            // 幂等性检查
            CommonResult<Boolean> processedResult = stockMqProcessService.isProcessed(returnNo);
            if (processedResult.isSuccess() && Boolean.TRUE.equals(processedResult.getData())) {
                log.info("销售退货库存消息已处理过，跳过: returnNo={}", returnNo);
                return;
            }

            log.info("收到销售退货库存增加消息: returnNo={}, warehouseCode={}, items={}", returnNo, warehouseCode, items.size());

            for (Map<String, Object> item : items) {
                String skuCode = (String) item.get("skuCode");
                String goodsCode = (String) item.get("goodsCode");
                BigDecimal stockQuantity = new BigDecimal(item.get("stockQuantity").toString());

                // 销售退货增加库存，成本价传0（不更新成本）
                CommonResult<Void> result = stockService.increaseStock(
                    warehouseCode, goodsCode, skuCode, stockQuantity, BigDecimal.ZERO, returnNo, "SALE_RETURN"
                );

                if (result.isSuccess()) {
                    log.info("销售退货库存增加成功: skuCode={}, goodsCode={}, stockQuantity={}",
                            skuCode, goodsCode, stockQuantity);
                } else {
                    log.error("销售退货库存增加失败: skuCode={}, msg={}", skuCode, result.getMessage());
                }
            }

            // 标记为已处理
            stockMqProcessService.markProcessed(returnNo);
            log.info("销售退货库存处理完成: returnNo={}", returnNo);
        } catch (Exception e) {
            log.error("处理销售退货库存消息失败", e);
            throw e;
        }
    }
}
