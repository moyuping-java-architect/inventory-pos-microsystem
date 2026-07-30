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
 * 销售订单释放库存MQ监听器
 *
 * <p>监听销售订单释放库存队列，在订单取消/驳回时释放预占库存：
 * available_quantity 增加，locked_quantity 减少
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SaleOrderReleaseListener {

    private final StockService stockService;
    private final StockMqProcessService stockMqProcessService;

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitMQConstant.SALE_ORDER_RELEASE_QUEUE)
    public void handleSaleOrderRelease(MqCommonMessage<?> message) {
        try {
            Map<String, Object> data = (Map<String, Object>) message.getData();
            String orderNo = (String) data.get("orderNo");
            String warehouseCode = (String) data.get("warehouseCode");
            List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");

            String processKey = orderNo + ":RELEASE";
            if (stockMqProcessService.isProcessed(processKey).getData()) {
                log.info("销售订单释放库存消息已处理过，跳过: orderNo={}", orderNo);
                return;
            }

            log.info("收到销售订单释放库存消息，单号: {}, 仓库: {}, 商品数量: {}",
                    orderNo, warehouseCode, items.size());

            List<StockBatchOperateItemDTO> releaseItems = new ArrayList<>();
            for (Map<String, Object> item : items) {
                String skuCode = (String) item.get("skuCode");
                BigDecimal stockQuantity = new BigDecimal(item.get("stockQuantity").toString());
                StockBatchOperateItemDTO releaseItem = new StockBatchOperateItemDTO();
                releaseItem.setWarehouseCode(warehouseCode);
                releaseItem.setSkuCode(skuCode);
                releaseItem.setQuantity(stockQuantity);
                releaseItems.add(releaseItem);
            }

            CommonResult<Void> result = stockService.batchReleaseStock(releaseItems, orderNo, "SALE_ORDER");
            if (!result.isSuccess()) {
                log.error("销售订单批量释放库存失败: msg={}", result.getMessage());
                throw new RuntimeException("销售订单释放库存失败: " + result.getMessage());
            }

            stockMqProcessService.markProcessed(processKey);
            log.info("销售订单释放库存处理完成，单号: {}", orderNo);
        } catch (Exception e) {
            log.error("处理销售订单释放库存消息失败", e);
            throw e;
        }
    }
}
