package com.psi.customer.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 召回建议 - 告诉老板"给谁发什么消息、能回多少钱"
 */
@Data
public class WinBackSuggestionDTO {

    /** 会员ID */
    private Integer memberId;

    /** 会员名 */
    private String memberName;

    /** 手机号/WhatsApp */
    private String phone;

    /** 流失等级 */
    private String churnLevel;

    /** 累计消费（这个客户之前值多少钱） */
    private BigDecimal totalSpent;

    /** 建议渠道：WHATSAPP / PHONE / IN_STORE */
    private String channel;

    /** 建议消息模板（可直接发WhatsApp） */
    private String messageTemplate;

    /** 预估回客单价 */
    private BigDecimal estimatedOrderValue;

    /** 召回理由（为什么选这个客户） */
    private String reason;

    /** 最近买的商品 */
    private String lastProductName;

    /** 距今天数 */
    private Integer daysSinceLastOrder;
}
