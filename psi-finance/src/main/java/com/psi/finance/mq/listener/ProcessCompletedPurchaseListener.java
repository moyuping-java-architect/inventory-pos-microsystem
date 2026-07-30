package com.psi.finance.mq.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.feign.DocFeignClient;
import com.psi.common.feign.DocFeignResponse;
import com.psi.common.idempotent.MessageIdempotencyService;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.result.CommonResult;
import com.psi.finance.entity.FinancePayableEntity;
import com.psi.finance.service.FinancePayableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 采购流程完成财务MQ监听器
 * 
 * <p>监听采购流程完成队列，当采购订单/采购入库审批通过后，
 * 生成财务应付账款记录
 * 
 * <p>通过 Feign 远程调用 psi-flow 获取单据数据，生成后再调用 complete 更新状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessCompletedPurchaseListener {

    private final DocFeignClient docFeignClient;
    private final FinancePayableService financePayableService;
    private final ObjectMapper objectMapper;
    private final MessageIdempotencyService messageIdempotencyService;

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitMQConstant.PROCESS_COMPLETED_PURCHASE_QUEUE)
    public void onProcessCompleted(MqCommonMessage<?> message) {
        Map<String, Object> data = (Map<String, Object>) message.getData();
        String bizId = (String) data.get("bizId");
        String messageId = message.getMessageId();
        log.info("收到采购流程完成财务消息: bizId={}, messageId={}", bizId, messageId);

        messageIdempotencyService.execute(messageId, () -> {
            try {
                DocFeignResponse doc = resolveDocFromMessage(data, bizId);
                if (doc == null) {
                    return null;
                }

                FinancePayableEntity payable = new FinancePayableEntity();
                payable.setSupplierCode(doc.getPartnerCode());
                payable.setSupplierName(doc.getPartnerName());
                payable.setSourceNo(doc.getDocNo());
                payable.setSourceType(doc.getDocType());
                payable.setBillDate(java.time.LocalDate.now().toString());

                if (doc.getItems() != null && !doc.getItems().isEmpty()) {
                    BigDecimal totalAmount = BigDecimal.ZERO;
                    for (DocFeignResponse.DocFeignItemResponse item : doc.getItems()) {
                        BigDecimal amount = item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO;
                        totalAmount = totalAmount.add(amount);
                    }
                    // 采购退货：应付金额为负（冲减）
                    if ("PURCHASE_RETURN".equals(doc.getDocType())) {
                        totalAmount = totalAmount.negate();
                    }
                    payable.setTotalAmount(totalAmount);
                    payable.setPaidAmount(BigDecimal.ZERO);
                    payable.setRemainAmount(totalAmount);
                } else {
                    payable.setTotalAmount(BigDecimal.ZERO);
                    payable.setPaidAmount(BigDecimal.ZERO);
                    payable.setRemainAmount(BigDecimal.ZERO);
                }

                financePayableService.save(payable);
                log.info("采购{}{}已生成: bizId={}, supplier={}, amount={}",
                        doc.getDocType(),
                        "PURCHASE_RETURN".equals(doc.getDocType()) ? "应付冲减" : "应付账款",
                        bizId, doc.getPartnerName(), payable.getTotalAmount());

                docFeignClient.complete(doc.getId());
                log.info("采购单据已完成: bizId={}", bizId);

            } catch (Exception e) {
                log.error("处理采购流程完成财务消息失败: bizId={}, error={}", bizId, e.getMessage(), e);
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
                log.info("采购流程完成财务：从MQ消息中解析单据数据: bizId={}", bizId);
                return doc;
            } catch (Exception e) {
                log.warn("采购流程完成财务：MQ中docData解析失败，降级Feign查询: bizId={}, error={}", bizId, e.getMessage());
            }
        }

        CommonResult<DocFeignResponse> result = docFeignClient.findByDocNo(bizId);
        if (!result.isSuccess() || result.getData() == null) {
            log.warn("采购流程完成财务：单据不存在或查询失败: bizId={}, msg={}", bizId, result.getMessage());
            return null;
        }
        log.info("采购流程完成财务：通过Feign查询单据数据: bizId={}", bizId);
        return result.getData();
    }
}