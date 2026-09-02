package com.psi.customer.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.event.BusinessEvent;
import com.psi.common.event.BusinessEventPublisher;
import com.psi.common.feign.DocFeignResponse;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.mq.LocalMqHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 销售流程消息桥接器。
 * <p>
 * 销售模块原来发的是流程审批消息，格式和字段都是为审批设计的，
 * 跟客户旅程没关系。这个类把它翻译成标准业务事件扔回总线，
 * 后面的活全部交给通用引擎。
 * <p>
 * 为什么不直接改销售模块发标准事件：改动面太大且要停机。桥接器是过渡方案，
 * 等销售模块空出手来自己发 {@code SALE.ORDER_APPROVED}，
 * 把这个类删掉就行，下游一行代码不用动 —— 这正是统一事件入口的价值。
 * <p>
 * 注意：它<b>不再直接写触点</b>。写不写、写成什么样，
 * 由 touchpoint_generate_rule 里老板配的规则决定。
 *
 * @author PSI
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SaleTouchpointListener {

    /** 事件编码，需与 business_event_dict 中的登记保持一致 */
    private static final String EVENT_ORDER_APPROVED = "SALE.ORDER_APPROVED";

    private static final String EVENT_ORDER_RETURNED = "SALE.ORDER_RETURNED";

    private final BusinessEventPublisher businessEventPublisher;
    private final ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    @LocalMqHandler(routingKey = RabbitMQConstant.PROCESS_COMPLETED_SALE_ROUTING_KEY)
    public void onSaleCompleted(MqCommonMessage<?> message) {
        Map<String, Object> data = (Map<String, Object>) message.getData();
        String bizId = (String) data.get("bizId");
        Integer processStatus = data.get("processStatus") != null
                ? Integer.parseInt(data.get("processStatus").toString()) : 2;

        // 流程驳回 → 没有成交，不发事件
        if (processStatus != null && processStatus == 3) {
            log.debug("销售事件桥接: 流程已驳回, 跳过, bizId={}", bizId);
            return;
        }

        Object docData = data.get("docData");
        if (docData == null || docData.toString().trim().isEmpty()) {
            log.warn("销售事件桥接: docData 为空, 跳过, bizId={}", bizId);
            return;
        }

        DocFeignResponse doc;
        try {
            doc = objectMapper.readValue(docData.toString(), DocFeignResponse.class);
        } catch (Exception e) {
            log.warn("销售事件桥接: docData 解析失败, bizId={}, error={}", bizId, e.getMessage());
            return;
        }

        // 出库单不代表新的成交，跳过；退货单单独发退货事件
        String docType = doc.getDocType();
        if ("SALE_OUT".equals(docType)) {
            return;
        }

        Long customerId = parseCustomerId(doc.getPartnerId(), bizId);
        if (customerId == null) {
            return;
        }

        BigDecimal amount = doc.getTotalAmount() != null ? doc.getTotalAmount() : BigDecimal.ZERO;
        boolean isReturn = "SALE_RETURN".equals(docType);
        String eventCode = isReturn ? EVENT_ORDER_RETURNED : EVENT_ORDER_APPROVED;

        BusinessEvent event = BusinessEvent
                .of(eventCode, BusinessEvent.SUBJECT_CUSTOMER, customerId)
                .bizKey(doc.getDocNo())
                .put("docNo", doc.getDocNo())
                .put("currency", doc.getCurrencyCode())
                .put(isReturn ? "returnAmount" : "orderAmount", amount);
        event.setTenantId(message.getTenantId());
        event.setOperator("SYSTEM_AUTO");

        businessEventPublisher.publish(event);

        log.info("销售事件桥接: 已转标准事件, eventCode={}, customerId={}, docNo={}",
                eventCode, customerId, doc.getDocNo());
    }

    /**
     * 单据上的往来单位ID即客户ID。
     */
    private Long parseCustomerId(String partnerId, String bizId) {
        if (partnerId == null || partnerId.trim().isEmpty()) {
            log.warn("销售事件桥接: 单据无客户ID, 跳过, bizId={}", bizId);
            return null;
        }
        try {
            return Long.parseLong(partnerId.trim());
        } catch (NumberFormatException e) {
            log.warn("销售事件桥接: 客户ID格式异常: {}, bizId={}", partnerId, bizId);
            return null;
        }
    }
}
