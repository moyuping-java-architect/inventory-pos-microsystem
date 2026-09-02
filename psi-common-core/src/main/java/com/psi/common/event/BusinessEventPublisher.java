package com.psi.common.event;

import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.context.UserContext;
import com.psi.common.context.UserInfo;
import com.psi.common.message.MessageFactory;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.message.MqMessageFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 业务事件发布器 —— 业务模块对接客户旅程的唯一入口。
 * <p>
 * 各业务模块只需注入本类并调用 {@link #publish(BusinessEvent)}，
 * 不需要知道下游有触点、规则、旅程这些概念。所有事件走同一个
 * routingKey，具体种类由事件体内的 eventCode 区分，
 * 因此新增业务事件永远不需要改动 MQ 配置或分发器。
 *
 * <pre>
 * businessEventPublisher.publish(
 *     BusinessEvent.of("SALE.ORDER_APPROVED", BusinessEvent.SUBJECT_CUSTOMER, customerId)
 *                  .bizKey(docNo)
 *                  .put("docNo", docNo)
 *                  .put("orderAmount", amount));
 * </pre>
 *
 * @author PSI
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessEventPublisher {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MqMessageFacade mqMessageFacade;

    /**
     * 发布业务事件。
     * <p>
     * 内部已做全量异常吞没：事件发布失败不允许影响主业务流程。
     * 缺少 eventCode 或 subjectId 的事件会被直接丢弃并记警告，
     * 因为这类事件无法归属到任何客户，下游也无从处理。
     * <p>
     * <b>事务安全</b>：若调用方处于事务中，事件会被推迟到事务成功提交之后
     * 才真正投递。原因是下游旅程引擎要回查 member_info / sale_order_main
     * 的汇总值来算指标，事务未提交时查到的是旧数据，会导致
     * 「累计满5000升银卡」这类阈值规则漏判或错判；事务回滚时事件也不该发出去。
     *
     * @param event 业务事件
     */
    public void publish(BusinessEvent event) {
        if (event == null) {
            return;
        }
        if (event.getEventCode() == null || event.getEventCode().isBlank()) {
            log.warn("业务事件发布失败: eventCode 为空");
            return;
        }
        if (event.getSubjectId() == null) {
            log.debug("业务事件跳过: 无主体ID, eventCode={}", event.getEventCode());
            return;
        }

        // 事件里的租户/操作人来自当前线程上下文，必须在原线程先补齐
        fillDefaults(event);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doPublish(event);
                }

                @Override
                public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) {
                        log.debug("业务事件因事务未提交而丢弃: eventCode={}, subjectId={}",
                                event.getEventCode(), event.getSubjectId());
                    }
                }
            });
            log.debug("业务事件已挂载到事务提交后发布: eventCode={}, subjectId={}",
                    event.getEventCode(), event.getSubjectId());
            return;
        }

        doPublish(event);
    }

    /**
     * 真正投递到消息总线。
     */
    private void doPublish(BusinessEvent event) {
        try {
            MqCommonMessage<BusinessEvent> message = MessageFactory.create(
                    event,
                    RabbitMQConstant.BUSINESS_EVENT_EXCHANGE,
                    RabbitMQConstant.BUSINESS_EVENT_ROUTING_KEY,
                    event.getEventCode()
            );

            mqMessageFacade.sendAsync(message);

            log.info("业务事件已发布: eventCode={}, subjectType={}, subjectId={}, bizKey={}",
                    event.getEventCode(), event.getSubjectType(),
                    event.getSubjectId(), event.getBizKey());
        } catch (Exception e) {
            log.error("业务事件发布异常: eventCode={}, subjectId={}, error={}",
                    event.getEventCode(), event.getSubjectId(), e.getMessage(), e);
        }
    }

    /**
     * 补齐发生时间、租户、操作人等公共字段。
     */
    private void fillDefaults(BusinessEvent event) {
        if (event.getOccurTime() == null) {
            event.setOccurTime(LocalDateTime.now().format(TIME_FORMATTER));
        }
        if (event.getSubjectType() == null) {
            event.setSubjectType(BusinessEvent.SUBJECT_CUSTOMER);
        }

        UserInfo userInfo = UserContext.get();
        if (userInfo != null) {
            if (event.getTenantId() == null) {
                event.setTenantId(userInfo.getTenantId());
            }
            if (event.getOperator() == null && userInfo.getUpdateUserId() != null) {
                event.setOperator(String.valueOf(userInfo.getUpdateUserId()));
            }
        }
    }
}
