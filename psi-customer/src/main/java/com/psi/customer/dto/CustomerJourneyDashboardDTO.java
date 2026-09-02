package com.psi.customer.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 客户增长看板 - 老板打开第一眼看到的概览
 * 核心卖点："你知道上个月来过的客户有几个这个月没回来吗？"
 */
@Data
public class CustomerJourneyDashboardDTO {

    // ========== 概览数字 ==========

    /** 总会员数 */
    private Integer totalMembers;

    /** 活跃会员（上次购买在预期周期内） */
    private Integer activeMembers;

    /** 流失风险会员（上次购买超出1.5倍周期） */
    private Integer atRiskMembers;

    /** 已流失会员（上次购买超出4倍周期或超过90天） */
    private Integer churnedMembers;

    /** 新会员（不足2次购买，且上次购买在30天内） */
    private Integer newMembers;

    // ========== 财务影响 ==========

    /** 流失风险客户的上月消费总额（=不召回就丢掉的钱） */
    private BigDecimal revenueAtRisk;

    /** 本月总销售额 */
    private BigDecimal totalRevenueThisMonth;

    /** 上月总销售额 */
    private BigDecimal totalRevenueLastMonth;

    /** 客单价 */
    private BigDecimal avgOrderValue;

    // ========== 趋势 ==========

    /** 环比增长率 */
    private BigDecimal growthRate;

    /** 复购率（有2次以上购买的会员占比） */
    private BigDecimal repurchaseRate;

    // ========== 待跟进 ==========

    /** 待跟进触点数（follow_up_done=0） */
    private Integer pendingFollowUps;

    // ========== 流失预警列表（前10条） ==========

    private List<ChurnAlertDTO> topChurnAlerts;

    // ========== Top客户（前5条） ==========

    private List<MemberSummaryDTO> topCustomers;
}
