package com.psi.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 客户/会员聚合指标实体。
 * <p>
 * 这张表的列 = 规则条件构建器可选的字段集合，所以列必须一次设计够。
 * 新增一个指标维度需要改代码，这是零代码方案划定的边界之一。
 * <p>
 * 数据来源：业务事件到达时增量刷新，也可由批处理全量重算。
 *
 * @author PSI
 */
@Data
@TableName("customer_metrics")
public class CustomerMetricsEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private String tenantId;

    /** 主体类型：CUSTOMER / MEMBER */
    @TableField("subject_type")
    private String subjectType;

    /** 主体ID */
    @TableField("subject_id")
    private Long subjectId;

    /** 累计消费金额 */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /** 累计订单数 */
    @TableField("order_count")
    private Integer orderCount;

    /** 首单时间 */
    @TableField("first_order_time")
    private String firstOrderTime;

    /** 末单时间 */
    @TableField("last_order_time")
    private String lastOrderTime;

    /** 平均客单价 */
    @TableField("avg_order_amount")
    private BigDecimal avgOrderAmount;

    /** 最大单笔金额 */
    @TableField("max_order_amount")
    private BigDecimal maxOrderAmount;

    /** 最近一单金额 */
    @TableField("last_order_amount")
    private BigDecimal lastOrderAmount;

    /** 平均购买间隔天数 */
    @TableField("avg_interval_days")
    private BigDecimal avgIntervalDays;

    /** 距上次消费天数 */
    @TableField("days_since_last")
    private Integer daysSinceLast;

    /** 退货次数 */
    @TableField("return_count")
    private Integer returnCount;

    /** 累计退货金额 */
    @TableField("return_amount")
    private BigDecimal returnAmount;

    /** 当前欠款金额 */
    @TableField("unpaid_amount")
    private BigDecimal unpaidAmount;

    /** 累计触点数 */
    @TableField("touchpoint_count")
    private Integer touchpointCount;

    /** 最近触点时间 */
    @TableField("last_touchpoint_time")
    private String lastTouchpointTime;

    /** 当前会员等级（MEMBER 主体用） */
    @TableField("member_level")
    private String memberLevel;

    /** 当前积分（MEMBER 主体用） */
    @TableField("points")
    private Integer points;

    @TableField("create_time")
    private String createTime;

    @TableField("update_time")
    private String updateTime;
}
