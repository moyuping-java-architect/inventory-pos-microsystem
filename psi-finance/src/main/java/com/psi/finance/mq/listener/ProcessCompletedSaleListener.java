package com.psi.finance.mq.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.feign.DocFeignClient;
import com.psi.common.feign.DocFeignResponse;
import com.psi.common.idempotent.MessageIdempotencyService;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.result.CommonResult;
import com.psi.finance.entity.FinanceReceivableEntity;
import com.psi.finance.service.FinanceReceivableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 销售流程完成财务MQ监听器
 * 
 * <p>监听销售流程完成队列，当销售订单/销售出库审批通过后，
 * 生成财务应收账款记录
 * 
 * <p>通过 Feign 远程调用 psi-flow 获取单据数据，生成后再调用 complete 更新状态
 */
@Slf4j
// 暂不注册：该监听器与 psi-sale 的 SaleProcessCompletedListener 竞争消费
// PROCESS_COMPLETED_SALE_QUEUE，会导致销售正式数据无法生成。
// 销售应收改由 SaleOutFinanceListener 监听 SALE_OUT_FINANCE_QUEUE 处理。
// @Component
@RequiredArgsConstructor
public class ProcessCompletedSaleListener {

    private final DocFeignClient docFeignClient;
    private final FinanceReceivableService financeReceivableService;
    private final ObjectMapper objectMapper;
    private final MessageIdempotencyService messageIdempotencyService;

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitMQConstant.PROCESS_COMPLETED_SALE_QUEUE)
    public void onProcessCompleted(MqCommonMessage<?> message) {
        Map<String, Object> data = (Map<String, Object>) message.getData();
        String bizId = (String) data.get("bizId");
        String messageId = message.getMessageId();
        log.info("收到销售流程完成财务消息: bizId={}, messageId={}", bizId, messageId);

        messageIdempotencyService.execute(messageId, () -> {
            try {
                DocFeignResponse doc = resolveDocFromMessage(data, bizId);
                if (doc == null) {
                    return null;
                }

                FinanceReceivableEntity receivable = new FinanceReceivableEntity();
                receivable.setCustomerCode(doc.getPartnerCode());
                receivable.setCustomerName(doc.getPartnerName());
                receivable.setSourceNo(doc.getDocNo());
                receivable.setSourceType(doc.getDocType());
                receivable.setBillDate(java.time.LocalDate.now().toString());

                if (doc.getItems() != null && !doc.getItems().isEmpty()) {
                    BigDecimal totalAmount = BigDecimal.ZERO;
                    for (DocFeignResponse.DocFeignItemResponse item : doc.getItems()) {
                        BigDecimal amount = item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO;
                        totalAmount = totalAmount.add(amount);
                    }
                    // 销售退货：应收金额为负（冲减）
                    if ("SALE_RETURN".equals(doc.getDocType())) {
                        totalAmount = totalAmount.negate();
                    }
                    receivable.setTotalAmount(totalAmount);
                    receivable.setPaidAmount(BigDecimal.ZERO);
                    receivable.setRemainAmount(totalAmount);
                } else {
                    receivable.setTotalAmount(BigDecimal.ZERO);
                    receivable.setPaidAmount(BigDecimal.ZERO);
                    receivable.setRemainAmount(BigDecimal.ZERO);
                }

                financeReceivableService.save(receivable);
                log.info("销售{}{}已生成: bizId={}, customer={}, amount={}",
                        doc.getDocType(),
                        "SALE_RETURN".equals(doc.getDocType()) ? "应收冲减" : "应收账款",
                        bizId, doc.getPartnerName(), receivable.getTotalAmount());

                docFeignClient.complete(doc.getId());
                log.info("销售单据已完成: bizId={}", bizId);

            } catch (Exception e) {
                log.error("处理销售流程完成财务消息失败: bizId={}, error={}", bizId, e.getMessage(), e);
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
                log.info("销售流程完成财务：从MQ消息中解析单据数据: bizId={}", bizId);
                return doc;
            } catch (Exception e) {
                log.warn("销售流程完成财务：MQ中docData解析失败，降级Feign查询: bizId={}, error={}", bizId, e.getMessage());
            }
        }

        CommonResult<DocFeignResponse> result = docFeignClient.findByDocNo(bizId);
        if (!result.isSuccess() || result.getData() == null) {
            log.warn("销售流程完成财务：单据不存在或查询失败: bizId={}, msg={}", bizId, result.getMessage());
            return null;
        }
        log.info("销售流程完成财务：通过Feign查询单据数据: bizId={}", bizId);
        return result.getData();
    }
}