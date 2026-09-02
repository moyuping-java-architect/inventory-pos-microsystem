package com.psi.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.customer.dto.SaleOrderEventRow;
import com.psi.customer.dto.SubjectMetricsRow;
import com.psi.customer.entity.CustomerMetricsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 客户聚合指标 Mapper。规则条件判断的数据源。
 * <p>
 * 这里的聚合 SQL 全部支持 asOf 参数（可为 null）：传时间就是「截至那一刻」的指标，
 * 传 null 就是当前指标。存量回溯时必须传时间点，否则拿当前累计值去判历史订单，
 * 会把「累计满5000升银卡」这种规则在客户第一单上就误判命中。
 *
 * @author PSI
 */
@Mapper
public interface CustomerMetricsMapper extends BaseMapper<CustomerMetricsEntity> {

    /**
     * B2B 客户订单指标。数据源 sale_order_main，口径与看板保持一致
     * （del_flag=0 且 order_status IN (2,3,5) 视为有效成交）。
     *
     * @param customerId 客户ID
     * @param asOf       截止时间，null 表示算到当前
     */
    @Select("SELECT " +
            "COALESCE(SUM(o.pay_amount), 0) AS totalAmount, " +
            "COUNT(*) AS orderCount, " +
            "DATE_FORMAT(MIN(o.create_time), '%Y-%m-%d %H:%i:%s') AS firstOrderTime, " +
            "DATE_FORMAT(MAX(o.create_time), '%Y-%m-%d %H:%i:%s') AS lastOrderTime, " +
            "COALESCE(AVG(o.pay_amount), 0) AS avgOrderAmount, " +
            "COALESCE(MAX(o.pay_amount), 0) AS maxOrderAmount, " +
            "DATEDIFF(IFNULL(#{asOf,jdbcType=VARCHAR}, NOW()), MAX(o.create_time)) AS daysSinceLast, " +
            "CASE WHEN COUNT(*) > 1 " +
            "  THEN ROUND(DATEDIFF(MAX(o.create_time), MIN(o.create_time)) / (COUNT(*) - 1), 1) " +
            "  ELSE NULL END AS avgIntervalDays " +
            "FROM sale_order_main o " +
            "WHERE o.del_flag = 0 AND o.order_status IN (2, 3, 5) " +
            "AND o.customer_id = #{customerId} " +
            "AND (#{asOf,jdbcType=VARCHAR} IS NULL OR o.create_time <= #{asOf,jdbcType=VARCHAR})")
    SubjectMetricsRow selectCustomerOrderMetrics(@Param("customerId") Long customerId,
                                                 @Param("asOf") String asOf);

    /**
     * 客户最近一单金额。
     */
    @Select("SELECT o.pay_amount FROM sale_order_main o " +
            "WHERE o.del_flag = 0 AND o.order_status IN (2, 3, 5) " +
            "AND o.customer_id = #{customerId} " +
            "AND (#{asOf,jdbcType=VARCHAR} IS NULL OR o.create_time <= #{asOf,jdbcType=VARCHAR}) " +
            "ORDER BY o.create_time DESC, o.id DESC LIMIT 1")
    BigDecimal selectCustomerLastOrderAmount(@Param("customerId") Long customerId,
                                             @Param("asOf") String asOf);

    /**
     * B2C 会员指标。收银台的订单明细在 POS 端库里，主库只有 member_info 上的累计值，
     * 所以会员指标直接读会员档案的汇总字段，由收银结算时回写。
     *
     * @param memberId 会员ID
     */
    @Select("SELECT " +
            "COALESCE(m.total_consume, 0) AS totalAmount, " +
            "COALESCE(m.total_orders, 0) AS orderCount, " +
            "DATE_FORMAT(m.register_time, '%Y-%m-%d %H:%i:%s') AS firstOrderTime, " +
            "DATE_FORMAT(m.last_consume_time, '%Y-%m-%d %H:%i:%s') AS lastOrderTime, " +
            "COALESCE(m.total_consume / NULLIF(m.total_orders, 0), 0) AS avgOrderAmount, " +
            "DATEDIFF(NOW(), m.last_consume_time) AS daysSinceLast, " +
            "m.level_name AS memberLevel, " +
            "COALESCE(m.points, 0) AS points " +
            "FROM member_info m WHERE m.del_flag = 0 AND m.id = #{memberId}")
    SubjectMetricsRow selectMemberMetrics(@Param("memberId") Long memberId);

    /**
     * 触点统计（B2B 客户）。
     */
    @Select("SELECT COUNT(*) AS touchpointCount, " +
            "DATE_FORMAT(MAX(t.contact_time), '%Y-%m-%d %H:%i:%s') AS lastTouchpointTime " +
            "FROM customer_touchpoint t " +
            "WHERE t.del_flag = 0 AND t.customer_id = #{subjectId}")
    SubjectMetricsRow selectCustomerTouchpointStat(@Param("subjectId") Long subjectId);

    /**
     * 触点统计（B2C 会员）。
     */
    @Select("SELECT COUNT(*) AS touchpointCount, " +
            "DATE_FORMAT(MAX(t.contact_time), '%Y-%m-%d %H:%i:%s') AS lastTouchpointTime " +
            "FROM customer_touchpoint t " +
            "WHERE t.del_flag = 0 AND t.member_id = #{subjectId}")
    SubjectMetricsRow selectMemberTouchpointStat(@Param("subjectId") Long subjectId);

    /**
     * 按主体查已落库的指标。
     */
    @Select("SELECT * FROM customer_metrics " +
            "WHERE subject_type = #{subjectType} AND subject_id = #{subjectId} LIMIT 1")
    CustomerMetricsEntity selectBySubject(@Param("subjectType") String subjectType,
                                          @Param("subjectId") Long subjectId);

    /**
     * 全部有成交记录的客户ID，批量重算用。
     */
    @Select("SELECT DISTINCT customer_id FROM sale_order_main " +
            "WHERE del_flag = 0 AND order_status IN (2, 3, 5) AND customer_id IS NOT NULL")
    List<Long> selectAllCustomerIds();

    /**
     * 全部有效会员ID，批量重算用。
     */
    @Select("SELECT id FROM member_info WHERE del_flag = 0")
    List<Long> selectAllMemberIds();

    /**
     * 历史有效销售订单，按时间正序，存量回溯用。
     */
    @Select("SELECT o.customer_id AS customerId, o.order_no AS docNo, " +
            "o.pay_amount AS orderAmount, o.currency_code AS currency, " +
            "DATE_FORMAT(o.create_time, '%Y-%m-%d %H:%i:%s') AS occurTime " +
            "FROM sale_order_main o " +
            "WHERE o.del_flag = 0 AND o.order_status IN (2, 3, 5) AND o.customer_id IS NOT NULL " +
            "ORDER BY o.create_time ASC, o.id ASC")
    List<SaleOrderEventRow> selectApprovedSaleOrders();
}
