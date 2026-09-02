package com.psi.customer.dto;

import lombok.Data;

/**
 * 触点录入DTO - 老板/店员手动记录一次客户接触
 * 也可以从 WhatsApp Bot 自动写入
 */
@Data
public class CustomerTouchpointDTO {

    /** 会员ID（二选一：memberId 或 customerId） */
    private Integer memberId;

    /** 客户ID（B2B大客户用） */
    private Integer customerId;

    /** 触点类型：INQUIRY / NEGOTIATION / WHATSAPP_MSG / STORE_VISIT / PHONE_CALL / SOCIAL / OTHER */
    private String touchpointType;

    /** 渠道：WHATSAPP / PHONE / IN_STORE / FACEBOOK / OTHER */
    private String channel;

    /** 接触时间（不传则自动取当前时间） */
    private String contactTime;

    /** 触点摘要 */
    private String summary;

    /** 客户意图：BUY / COMPARE / COMPLAINT / INFO / CHITCHAT */
    private String intent;

    /** 跟进动作 */
    private String followUp;

    /** 记录人 */
    private String operator;
}
