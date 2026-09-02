package com.psi.customer.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 会员消费摘要 - 用于 Top客户列表 和 看板
 * 由 SQL 聚合查询填充（order_main + member JOIN）
 */
@Data
public class MemberSummaryDTO {

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

    /** 客单价 */
    private BigDecimal avgOrderValue;

    /** 最近购买时间 */
    private String lastOrderTime;

    /** 首次购买时间 */
    private String firstOrderTime;

    /** 距今多少天没买 */
    private Integer daysSinceLastOrder;

    /** 最近购买商品 */
    private String lastProductName;

    /** 平均购买间隔（天） */
    private Double avgIntervalDays;

    /** 流失等级 */
    private String churnLevel;

    /** 等级排序权重（活跃=1，新=2，风险=3，高险=4，流失=5） */
    private Integer churnSort;
}
