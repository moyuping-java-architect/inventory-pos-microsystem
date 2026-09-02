package com.psi.customer.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.context.UserContext;
import com.psi.common.context.UserInfo;
import com.psi.common.event.BusinessEvent;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.mq.LocalMqHandler;
import com.psi.customer.engine.CustomerJourneyEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 业务事件统一监听器 —— 客户旅程的唯一消费入口。
 * <p>
 * 全系统所有客户相关事件都走同一个 routingKey 到这里，
 * 具体是哪种事件由消息体里的 eventCode 区分。这样做的好处很直接：
 * 新增一种业务事件时，MQ 配置不用改、分发器不用改、这个类也不用改，
 * 只要往 business_event_dict 里加一行，老板就能在页面上给它配规则。
 * <p>
 * 对比原来的做法 —— 每种事件写一个 Listener，24 种事件 24 个类，
 * 而且每加一种玩法都得改代码、重新打包、重新部署。
 *
 * @author PSI
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessEventListener {

    private final CustomerJourneyEngine journeyEngine;
    private final ObjectMapper objectMapper;

    /**
     * 消费业务事件。
     *
     * @param message MQ 消息，data 为 {@link BusinessEvent}
     */
    @LocalMqHandler(routingKey = RabbitMQConstant.BUSINESS_EVENT_ROUTING_KEY)
    public void onBusinessEvent(MqCommonMessage<?> message) {
        BusinessEvent event = extractEvent(message);
        if (event == null) {
            return;
        }

        // 单机版本地分发走的是内存对象，租户信息在消息头上，补回事件里
        if (event.getTenantId() == null) {
            event.setTenantId(message.getTenantId());
        }

        // 设置租户上下文（MyBatis 拦截器与后续查询依赖）
        UserInfo userInfo = new UserInfo();
        userInfo.setTenantId(message.getTenantId());
        userInfo.setUpdateUserId(message.getOperatorId());
        UserContext.set(userInfo);

        try {
            log.info("旅程事件到达: eventCode={}, subjectType={}, subjectId={}, bizKey={}",
                    event.getEventCode(), event.getSubjectType(),
                    event.getSubjectId(), event.getBizKey());
            journeyEngine.handle(event);
        } finally {
            UserContext.clearAll();
        }
    }

    /**
     * 取出事件体。
     * <p>
     * 单机本地分发时 data 就是原对象；将来换成真 RabbitMQ 会被反序列化成 Map，
     * 这里两种情况都兜住，切换 Broker 不用回来改这段。
     */
    private BusinessEvent extractEvent(MqCommonMessage<?> message) {
        if (message == null || message.getData() == null) {
            return null;
        }
        Object data = message.getData();
        if (data instanceof BusinessEvent event) {
            return event;
        }
        try {
            return objectMapper.convertValue(data, BusinessEvent.class);
        } catch (Exception e) {
            log.warn("业务事件解析失败: messageId={}, error={}", message.getMessageId(), e.getMessage());
            return null;
        }
    }
}
