package com.psi.common.event;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 标准业务事件载体 —— 客户旅程引擎的统一输入。
 * <p>
 * 各业务模块在状态变更时构造本对象扔到事件总线即可，
 * 不需要关心下游会不会生成触点、会不会推进旅程。
 * 事件的具体种类由 {@link #eventCode} 区分，取值必须存在于
 * business_event_dict 表中，否则规则配置页选不到它。
 *
 * <pre>
 * BusinessEvent.of("SALE.ORDER_APPROVED", SUBJECT_CUSTOMER, customerId)
 *              .bizKey(docNo)
 *              .put("orderAmount", amount)
 *              .put("docNo", docNo);
 * </pre>
 *
 * @author PSI
 */
@Data
public class BusinessEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主体类型：B2B 客户 */
    public static final String SUBJECT_CUSTOMER = "CUSTOMER";

    /** 主体类型：B2C 会员 */
    public static final String SUBJECT_MEMBER = "MEMBER";

    /** 事件编码，如 SALE.ORDER_APPROVED，取值见 business_event_dict */
    private String eventCode;

    /** 主体类型：CUSTOMER / MEMBER */
    private String subjectType;

    /** 主体ID：客户ID 或 会员ID。为空的事件无法生成触点，会被引擎直接丢弃 */
    private Long subjectId;

    /**
     * 业务唯一键（通常是单据号）。
     * 用于非 onceOnly 规则的幂等控制，防止同一单据重复生成触点。
     */
    private String bizKey;

    /** 事件发生时间 yyyy-MM-dd HH:mm:ss */
    private String occurTime;

    /** 租户ID */
    private String tenantId;

    /** 操作人 */
    private String operator;

    /** 事件载荷，字段名需与 business_event_dict.payload_fields 声明一致 */
    private Map<String, Object> payload = new HashMap<>();

    /**
     * 快速构造。
     *
     * @param eventCode   事件编码
     * @param subjectType 主体类型
     * @param subjectId   主体ID
     */
    public static BusinessEvent of(String eventCode, String subjectType, Long subjectId) {
        BusinessEvent event = new BusinessEvent();
        event.setEventCode(eventCode);
        event.setSubjectType(subjectType);
        event.setSubjectId(subjectId);
        return event;
    }

    /**
     * 链式设置业务唯一键。
     */
    public BusinessEvent bizKey(String bizKey) {
        this.bizKey = bizKey;
        return this;
    }

    /**
     * 链式放入载荷字段。
     */
    public BusinessEvent put(String key, Object value) {
        if (key != null) {
            this.payload.put(key, value);
        }
        return this;
    }

    /**
     * 读取载荷字段，不存在返回 null。
     */
    public Object get(String key) {
        return this.payload == null ? null : this.payload.get(key);
    }
}
