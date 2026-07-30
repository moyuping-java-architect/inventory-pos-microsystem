package com.psi.finance.mq.listener;

import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.idempotent.MessageIdempotencyService;
import com.psi.common.message.MqCommonMessage;
import com.psi.finance.entity.FinanceReceivableEntity;
import com.psi.finance.service.FinanceDailyLedgerService;
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
 * 销售出库财务MQ监听器
 *
 * <p>监听销售出库消息，执行财务记账操作：
 * <ul>
 *   <li>接收销售出库消息</li>
 *   <li>记录销售日报</li>
 *   <li>生成应收账款</li>
 *   <li>记录财务流水</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SaleOutFinanceListener {

    private final FinanceReceivableService financeReceivableService;
    private final FinanceDailyLedgerService financeDailyLedgerService;
    private final MessageIdempotencyService messageIdempotencyService;

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitMQConstant.SALE_OUT_FINANCE_QUEUE)
    public void handleSaleOut(MqCommonMessage<?> message) {
        String messageId = message.getMessageId();
        messageIdempotencyService.execute(messageId, () -> {
            try {
                Map<String, Object> data = (Map<String, Object>) message.getData();
                String outNo = (String) data.get("outNo");
                String orderNo = (String) data.get("orderNo");
                String customerCode = (String) data.get("customerCode");
                String customerName = (String) data.get("customerName");
                BigDecimal totalAmount = toBigDecimal(data.get("totalAmount"));
                BigDecimal payAmount = toBigDecimal(data.get("payAmount"));
                String payType = data.get("payType") != null ? data.get("payType").toString() : "1";
                String warehouseCode = (String) data.get("warehouseCode");

                log.info("收到销售出库财务消息，单号: {}, 客户: {}, 金额: {}",
                        outNo, customerName, totalAmount);

                // 1. 记录销售日报
                financeDailyLedgerService.recordSale(outNo, customerCode, customerName,
                        totalAmount, payAmount, payType, warehouseCode);

                // 2. 生成应收账款
                FinanceReceivableEntity receivable = new FinanceReceivableEntity();
                receivable.setStoreCode(warehouseCode);
                receivable.setStoreName(warehouseCode);
                receivable.setCustomerCode(customerCode);
                receivable.setCustomerName(customerName);
                receivable.setSourceNo(outNo);
                receivable.setSourceType("SALE_OUT");
                receivable.setBillDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                receivable.setDueDate(LocalDate.now().plusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                receivable.setTotalAmount(totalAmount);
                receivable.setPaidAmount(payAmount != null ? payAmount : BigDecimal.ZERO);
                receivable.setRemainAmount(totalAmount.subtract(payAmount != null ? payAmount : BigDecimal.ZERO));
                financeReceivableService.save(receivable);

                log.info("销售出库财务处理完成，单号: {}, 应收账款: {}", outNo, receivable.getTotalAmount());
            } catch (Exception e) {
                log.error("处理销售出库财务消息失败", e);
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
