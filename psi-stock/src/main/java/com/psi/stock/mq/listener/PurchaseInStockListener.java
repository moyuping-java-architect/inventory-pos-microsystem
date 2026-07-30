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
 * 采购入库库存监听器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PurchaseInStockListener {

    private final StockService stockService;
    private final StockMqProcessService stockMqProcessService;

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitMQConstant.PURCHASE_IN_STOCK_QUEUE)
    public void handlePurchaseIn(MqCommonMessage<?> message) {
        Map<String, Object> data = (Map<String, Object>) message.getData();
        String inNo = (String) data.get("inNo");
        String warehouseCode = (String) data.get("warehouseCode");
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");

        log.info("收到采购入库库存增加消息: inNo={}, warehouseCode={}, items={}", inNo, warehouseCode, items == null ? 0 : items.size());

        // 幂等性检查：如果已经处理过，直接返回
        CommonResult<Boolean> processedResult = stockMqProcessService.isProcessed(inNo);
        if (processedResult.isSuccess() && Boolean.TRUE.equals(processedResult.getData())) {
            log.info("采购入库库存消息已处理过，跳过: inNo={}", inNo);
            return;
        }

        if (inNo == null || warehouseCode == null || items == null || items.isEmpty()) {
            log.warn("采购入库库存消息数据不完整，跳过避免阻塞队列: inNo={}, warehouseCode={}, items={}",
                    inNo, warehouseCode, items == null ? 0 : items.size());
            stockMqProcessService.markProcessed(inNo);
            return;
        }

        boolean allSuccess = true;
        for (Map<String, Object> item : items) {
            String skuCode = (String) item.get("skuCode");
            String goodsCode = (String) item.get("goodsCode");
            BigDecimal stockQuantity;
            try {
                stockQuantity = new BigDecimal(item.get("stockQuantity").toString());
            } catch (Exception e) {
                log.warn("采购入库库存数量转换失败，跳过: inNo={}, skuCode={}, stockQuantity={}", inNo, skuCode, item.get("stockQuantity"));
                allSuccess = false;
                continue;
            }
            BigDecimal costPrice = item.get("costPrice") != null ? new BigDecimal(item.get("costPrice").toString()) : BigDecimal.ZERO;

            try {
                CommonResult<Void> result = stockService.increaseStock(
                        warehouseCode, goodsCode, skuCode, stockQuantity, costPrice, inNo, "PURCHASE_IN"
                );
                if (result.isSuccess()) {
                    log.info("采购入库库存增加成功: inNo={}, skuCode={}, goodsCode={}, stockQuantity={}",
                            inNo, skuCode, goodsCode, stockQuantity);
                } else {
                    log.error("采购入库库存增加失败: inNo={}, skuCode={}, msg={}", inNo, skuCode, result.getMessage());
                    allSuccess = false;
                }
            } catch (Exception e) {
                log.error("采购入库库存增加异常: inNo={}, skuCode={}, error={}", inNo, skuCode, e.getMessage(), e);
                allSuccess = false;
            }
        }

        // 标记为已处理，避免异常消息一直阻塞队列
        stockMqProcessService.markProcessed(inNo);
        log.info("采购入库库存处理完成: inNo={}, allSuccess={}", inNo, allSuccess);
    }
}
