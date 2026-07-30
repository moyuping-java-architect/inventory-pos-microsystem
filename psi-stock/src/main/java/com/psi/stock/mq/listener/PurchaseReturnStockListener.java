package com.psi.stock.mq.listener;

import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.result.CommonResult;
import com.psi.stock.dto.StockBatchOperateItemDTO;
import com.psi.stock.service.StockService;
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
 * 采购退货库存监听器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PurchaseReturnStockListener {

    private final StockService stockService;
    private final StockMqProcessService stockMqProcessService;

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitMQConstant.PURCHASE_RETURN_STOCK_QUEUE)
    public void handlePurchaseReturn(MqCommonMessage<?> message) {
        try {
            Map<String, Object> data = (Map<String, Object>) message.getData();
            String returnNo = (String) data.get("returnNo");
            String warehouseCode = (String) data.get("warehouseCode");
            List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");

            // 幂等性检查
            CommonResult<Boolean> processedResult = stockMqProcessService.isProcessed(returnNo);
            if (processedResult.isSuccess() && Boolean.TRUE.equals(processedResult.getData())) {
                log.info("采购退货库存消息已处理过，跳过: returnNo={}", returnNo);
                return;
            }

            log.info("收到采购退货库存扣减消息: returnNo={}, warehouseCode={}, items={}", returnNo, warehouseCode, items.size());

            List<StockBatchOperateItemDTO> decreaseItems = new ArrayList<>();
            for (Map<String, Object> item : items) {
                String skuCode = (String) item.get("skuCode");
                BigDecimal stockQuantity = new BigDecimal(item.get("stockQuantity").toString());
                StockBatchOperateItemDTO decreaseItem = new StockBatchOperateItemDTO();
                decreaseItem.setWarehouseCode(warehouseCode);
                decreaseItem.setSkuCode(skuCode);
                decreaseItem.setQuantity(stockQuantity);
                decreaseItems.add(decreaseItem);
            }

            CommonResult<Void> result = stockService.batchDecreaseStock(decreaseItems, returnNo, "PURCHASE_RETURN");
            if (!result.isSuccess()) {
                log.error("采购退货批量库存扣减失败: msg={}", result.getMessage());
                throw new RuntimeException("采购退货库存扣减失败: " + result.getMessage());
            }
            log.info("采购退货批量库存扣减成功: returnNo={}, items={}", returnNo, decreaseItems.size());

            // 标记为已处理
            stockMqProcessService.markProcessed(returnNo);
            log.info("采购退货库存处理完成: returnNo={}", returnNo);
        } catch (Exception e) {
            log.error("处理采购退货库存消息失败", e);
            throw e;
        }
    }
}
