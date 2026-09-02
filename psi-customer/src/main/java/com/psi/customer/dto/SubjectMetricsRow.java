package com.psi.customer.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 主体指标聚合结果行。
 * <p>
 * 从业务表（sale_order_main / member_info / customer_touchpoint）现算出来的原始数值，
 * 由 {@code CustomerMetricsRefresher} 合并后落到 customer_metrics 表。
 * <p>
 * 字段名与 {@code CustomerMetricsEntity} 保持一致，方便直接搬运。
 *
 * @author PSI
 */
@Data
public class SubjectMetricsRow {

    /** 累计消费金额 */
    private BigDecimal totalAmount;

    /** 累计订单数 */
    private Integer orderCount;

    /** 首单时间 */
    private String firstOrderTime;

    /** 末单时间 */
    private String lastOrderTime;

    /** 平均客单价 */
    private BigDecimal avgOrderAmount;

    /** 最大单笔金额 */
    private BigDecimal maxOrderAmount;

    /** 最近一单金额 */
    private BigDecimal lastOrderAmount;

    /** 平均购买间隔天数 */
    private BigDecimal avgIntervalDays;

    /** 距上次消费天数 */
    private Integer daysSinceLast;

    /** 累计触点数 */
    private Integer touchpointCount;

    /** 最近触点时间 */
    private String lastTouchpointTime;

    /** 会员等级 */
    private String memberLevel;

    /** 积分 */
    private Integer points;
}
