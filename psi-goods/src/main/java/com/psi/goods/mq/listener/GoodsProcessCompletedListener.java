package com.psi.goods.mq.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.feign.DocFeignClient;
import com.psi.common.feign.DocFeignResponse;
import com.psi.common.feign.DocFeignResponse.DocFeignItemResponse;
import com.psi.common.idempotent.MessageIdempotencyService;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.result.CommonResult;
import com.psi.goods.entity.AdjustPriceItemEntity;
import com.psi.goods.entity.AdjustPriceMainEntity;
import com.psi.goods.entity.Goods;
import com.psi.goods.entity.GoodsSku;
import com.psi.goods.service.AdjustPriceService;
import com.psi.goods.service.GoodsService;
import com.psi.goods.service.GoodsSkuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 商品流程完成MQ监听器
 * 
 * <p>监听商品流程完成队列，当商品信息审批通过后，生成商品模块正式业务数据
 * 
 * <p>通过 Feign 远程调用 psi-flow 获取单据数据并更新状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoodsProcessCompletedListener {

    private final DocFeignClient docFeignClient;
    private final GoodsService goodsService;
    private final GoodsSkuService goodsSkuService;
    private final AdjustPriceService adjustPriceService;
    private final ObjectMapper objectMapper;
    private final MessageIdempotencyService messageIdempotencyService;

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitMQConstant.PROCESS_COMPLETED_GOODS_QUEUE)
    public void onProcessCompleted(MqCommonMessage<?> message) {
        Map<String, Object> data = (Map<String, Object>) message.getData();
        String bizId = (String) data.get("bizId");
        String messageId = message.getMessageId();
        log.info("收到商品流程完成消息: bizId={}, messageId={}", bizId, messageId);

        messageIdempotencyService.execute(messageId, () -> {
            try {
                DocFeignResponse doc = resolveDocFromMessage(data, bizId);
                if (doc == null) {
                    return null;
                }

                if ("ADJUST_PRICE".equals(doc.getDocType())) {
                    handleAdjustPrice(doc);
                } else {
                    handleGoodsCreate(doc);
                }

                log.info("商品单据正式数据已生成: bizId={}, docType={}", bizId, doc.getDocType());
            } catch (Exception e) {
                log.error("处理商品流程完成消息失败: bizId={}, error={}", bizId, e.getMessage(), e);
                throw e;
            }
            return null;
        });
    }

    private void handleGoodsCreate(DocFeignResponse doc) {
        Goods goods = new Goods();
        goods.setGoodsCode(doc.getPartnerCode());
        goods.setGoodsName(doc.getPartnerName());
        if (doc.getItems() != null && !doc.getItems().isEmpty()) {
            DocFeignItemResponse firstItem = doc.getItems().get(0);
            goods.setGoodsSpec(firstItem.getGoodsSpec());
            goods.setUnit(firstItem.getGoodsUnit());

            goodsService.save(goods);
            log.info("商品正式数据已生成: goodsCode={}", goods.getGoodsCode());

            GoodsSku sku = new GoodsSku();
            sku.setGoodsId(goods.getId());
            sku.setSkuCode(goods.getGoodsCode());
            sku.setSpecValues(goods.getGoodsSpec());
            sku.setSaleUnit(goods.getUnit());
            sku.setBaseUnit(goods.getUnit());
            sku.setCostPrice(BigDecimal.ZERO);
            sku.setSalePrice(firstItem.getUnitPrice());
            sku.setStockQty(firstItem.getQuantity().intValue());
            goodsSkuService.save(sku);

            log.info("商品SKU已生成: goodsId={}, skuCode={}", goods.getId(), sku.getSkuCode());
        } else {
            goodsService.save(goods);
            log.info("商品正式数据已生成（无SKU）: goodsCode={}", goods.getGoodsCode());
        }
    }

    private void handleAdjustPrice(DocFeignResponse doc) {
        if (doc.getItems() == null || doc.getItems().isEmpty()) {
            log.warn("调价单无明细: docNo={}", doc.getDocNo());
            return;
        }

        // 幂等：已存在则跳过
        if (adjustPriceService.getByAdjustNo(doc.getDocNo()) != null) {
            log.info("调价单已存在，跳过: docNo={}", doc.getDocNo());
            return;
        }

        // 保存调价单主表
        AdjustPriceMainEntity main = new AdjustPriceMainEntity();
        main.setAdjustNo(doc.getDocNo());
        main.setDocName(doc.getDocName());
        main.setShopCode(doc.getWarehouseCode());
        main.setShopName(doc.getWarehouseName());
        main.setAdjustDate(doc.getDocDate());
        main.setRemark(doc.getRemark());
        main.setStatus(2);

        // 构建明细
        java.util.List<AdjustPriceItemEntity> items = new java.util.ArrayList<>();
        int sort = 1;
        BigDecimal totalAmount = BigDecimal.ZERO;
        int updatedCount = 0;
        for (DocFeignItemResponse item : doc.getItems()) {
            GoodsSku sku = goodsSkuService.getOne(
                    new LambdaQueryWrapper<GoodsSku>()
                            .eq(GoodsSku::getSkuCode, item.getSkuCode() != null ? item.getSkuCode() : item.getGoodsCode())
            );
            AdjustPriceItemEntity adjustItem = new AdjustPriceItemEntity();
            adjustItem.setGoodsCode(item.getGoodsCode());
            adjustItem.setGoodsName(item.getGoodsName());
            adjustItem.setSkuCode(item.getSkuCode() != null ? item.getSkuCode() : item.getGoodsCode());
            adjustItem.setGoodsSpec(item.getGoodsSpec());
            adjustItem.setGoodsUnit(item.getGoodsUnit());
            adjustItem.setOldPrice(sku != null ? sku.getSalePrice() : BigDecimal.ZERO);
            adjustItem.setNewPrice(item.getUnitPrice());
            adjustItem.setQuantity(item.getQuantity() != null ? item.getQuantity() : BigDecimal.ONE);
            if (adjustItem.getNewPrice() != null && adjustItem.getQuantity() != null) {
                adjustItem.setAmount(adjustItem.getNewPrice().multiply(adjustItem.getQuantity()));
                totalAmount = totalAmount.add(adjustItem.getAmount());
            }
            adjustItem.setSortOrder(sort++);
            items.add(adjustItem);

            // 同步更新 SKU 销售价
            if (sku != null) {
                if (item.getUnitPrice() != null && item.getUnitPrice().compareTo(BigDecimal.ZERO) > 0) {
                    sku.setSalePrice(item.getUnitPrice());
                }
                goodsSkuService.updateById(sku);
                updatedCount++;
                log.info("调价已更新: skuCode={}, salePrice={}", sku.getSkuCode(), sku.getSalePrice());
            } else {
                log.warn("调价：SKU不存在: skuCode={}", item.getGoodsCode());
            }
        }
        main.setItemCount(items.size());
        main.setTotalAmount(totalAmount);
        adjustPriceService.saveAdjustPrice(main, items);

        log.info("调价单处理完成: docNo={}, updatedCount={}", doc.getDocNo(), updatedCount);
    }

    /**
     * 优先从 MQ 消息里取 docData（工作流已查好），没有则 Feign 查询（兼容老消息）
     */
    private DocFeignResponse resolveDocFromMessage(Map<String, Object> data, String bizId) {
        Object docData = data.get("docData");
        if (docData != null && !docData.toString().trim().isEmpty()) {
            try {
                DocFeignResponse doc = objectMapper.readValue(docData.toString(), DocFeignResponse.class);
                log.info("商品流程完成：从MQ消息中解析单据数据: bizId={}", bizId);
                return doc;
            } catch (Exception e) {
                log.warn("商品流程完成：MQ中docData解析失败，降级Feign查询: bizId={}, error={}", bizId, e.getMessage());
            }
        }

        CommonResult<DocFeignResponse> result = docFeignClient.findByDocNo(bizId);
        if (!result.isSuccess() || result.getData() == null) {
            log.warn("商品流程完成：单据不存在或查询失败: bizId={}, msg={}", bizId, result.getMessage());
            return null;
        }
        log.info("商品流程完成：通过Feign查询单据数据: bizId={}", bizId);
        return result.getData();
    }
}