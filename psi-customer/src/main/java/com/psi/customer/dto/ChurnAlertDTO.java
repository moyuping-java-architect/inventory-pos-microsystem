package com.psi.customer.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 流失预警 - 每个风险客户一条
 * 老板看到这个列表的直觉反应："能找回几个？多久回本？"
 */
@Data
public class ChurnAlertDTO {

    /** 会员ID */
    private Integer memberId;

    /** 会员名 */
    private String memberName;

    /** 手机号 */
    private String phone;

    /** 总购买次数 */
    private Integer totalOrders;

    /** 累计消费金额 */
    private BigDecimal totalSpent;

    /** 最近购买时间 */
    private String lastOrderTime;

    /** 距今天数 */
    private Integer daysSinceLastOrder;

    /** 平均购买间隔（天） */
    private Double avgIntervalDays;

    /** 流失等级：ACTIVE / AT_RISK / HIGH_RISK / CHURNED / NEW / NEW_CHURNED */
    private String churnLevel;

    /** 风险说明（一句话，给老板看的） */
    private String riskDescription;

    /** 建议召回动作 */
    private String suggestedAction;

    /** 最近购买的商品名（用于召回话术："上次买的可乐快喝完了吧？"） */
    private String lastProductName;
}
