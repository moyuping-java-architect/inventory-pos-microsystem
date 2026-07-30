package com.psi.finance.mq.listener;

import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.idempotent.MessageIdempotencyService;
import com.psi.common.message.MqCommonMessage;
import com.psi.finance.entity.FinanceReceivableEntity;
import com.psi.finance.service.FinanceReceivableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 销售退货财务MQ监听器
 *
 * <p>监听销售退货消息，生成应收冲减记录（负的应收账款）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SaleReturnFinanceListener {

    private final FinanceReceivableService financeReceivableService;
    private final MessageIdempotencyService messageIdempotencyService;

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitMQConstant.SALE_RETURN_FINANCE_QUEUE)
    public void handleSaleReturn(MqCommonMessage<?> message) {
        String messageId = message.getMessageId();
        messageIdempotencyService.execute(messageId, () -> {
            try {
                Map<String, Object> data = (Map<String, Object>) message.getData();
                String returnNo = (String) data.get("returnNo");
                String orderNo = (String) data.get("orderNo");
                String customerCode = (String) data.get("customerCode");
                String customerName = (String) data.get("customerName");
                BigDecimal totalAmount = toBigDecimal(data.get("totalAmount"));
                String warehouseCode = (String) data.get("warehouseCode");

                log.info("收到销售退货财务消息，单号: {}, 客户: {}, 金额: {}",
                        returnNo, customerName, totalAmount);

                // 生成应收冲减记录（金额为负）
                FinanceReceivableEntity receivable = new FinanceReceivableEntity();
                receivable.setStoreCode(warehouseCode);
                receivable.setStoreName(warehouseCode);
                receivable.setCustomerCode(customerCode);
                receivable.setCustomerName(customerName);
                receivable.setSourceNo(returnNo);
                receivable.setSourceType("SALE_RETURN");
                receivable.setBillDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                receivable.setDueDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                receivable.setTotalAmount(totalAmount.negate());
                receivable.setPaidAmount(BigDecimal.ZERO);
                receivable.setRemainAmount(totalAmount.negate());
                financeReceivableService.save(receivable);

                log.info("销售退货应收冲减已生成: returnNo={}, 冲减金额: {}", returnNo, receivable.getTotalAmount());
            } catch (Exception e) {
                log.error("处理销售退货财务消息失败", e);
                throw e;
            }
            return null;
        });
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            log.warn("金额转换失败: {}", value);
            return BigDecimal.ZERO;
        }
    }
}
