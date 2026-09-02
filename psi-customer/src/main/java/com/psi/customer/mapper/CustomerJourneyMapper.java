package com.psi.customer.mapper;

import com.psi.customer.dto.MemberJourneyDTO;
import com.psi.customer.dto.MemberSummaryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 客户旅程聚合 Mapper
 * 直接查询 sale_order_main / customer / sale_order_item / member_info 表
 * 后期拆微服务时替换为 Feign 调用 psi-sale / psi-member
 */
@Mapper
public interface CustomerJourneyMapper {

    /**
     * 所有客户的消费摘要（用于看板/流失分析/Top客户）
     * 聚合 sale_order_main by customer_id
     */
    @Select("SELECT " +
            "o.customer_id AS memberId, " +
            "o.customer_name AS memberName, " +
            "c.contact_phone AS phone, " +
            "COUNT(*) AS totalOrders, " +
            "COALESCE(SUM(o.pay_amount), 0) AS totalSpent, " +
            "COALESCE(SUM(o.pay_amount) / NULLIF(COUNT(*), 0), 0) AS avgOrderValue, " +
            "DATE_FORMAT(MAX(o.create_time), '%Y-%m-%d %H:%i:%s') AS lastOrderTime, " +
            "DATE_FORMAT(MIN(o.create_time), '%Y-%m-%d %H:%i:%s') AS firstOrderTime, " +
            "DATEDIFF(NOW(), MAX(o.create_time)) AS daysSinceLastOrder, " +
            "CASE " +
            "  WHEN COUNT(*) > 1 THEN ROUND(DATEDIFF(MAX(o.create_time), MIN(o.create_time)) / (COUNT(*) - 1), 1) " +
            "  ELSE NULL " +
            "END AS avgIntervalDays " +
            "FROM sale_order_main o " +
            "LEFT JOIN customer c ON c.id = o.customer_id AND c.del_flag = 0 " +
            "WHERE o.del_flag = 0 " +
            "AND o.order_status IN (2, 3, 5) " +
            "AND o.customer_id IS NOT NULL " +
            "GROUP BY o.customer_id, o.customer_name, c.contact_phone " +
            "ORDER BY totalSpent DESC")
    List<MemberSummaryDTO> selectCustomerOrderSummaries();

    /**
     * 从 customer 表补充无订单客户的基础信息（会员Id/姓名/电话）
     */
    @Select("SELECT id AS memberId, customer_name AS memberName, contact_phone AS phone, " +
            "0 AS totalOrders, 0 AS totalSpent, 0 AS avgOrderValue, " +
            "NULL AS lastOrderTime, NULL AS firstOrderTime, " +
            "NULL AS daysSinceLastOrder, NULL AS avgIntervalDays " +
            "FROM customer WHERE id = #{customerId} AND del_flag = 0")
    MemberSummaryDTO selectCustomerById(@Param("customerId") Long customerId);

    /**
     * Top N 客户（按累计消费）
     */
    @Select("SELECT " +
            "o.customer_id AS memberId, " +
            "o.customer_name AS memberName, " +
            "c.contact_phone AS phone, " +
            "COUNT(*) AS totalOrders, " +
            "COALESCE(SUM(o.pay_amount), 0) AS totalSpent, " +
            "COALESCE(SUM(o.pay_amount) / NULLIF(COUNT(*), 0), 0) AS avgOrderValue, " +
            "DATE_FORMAT(MAX(o.create_time), '%Y-%m-%d %H:%i:%s') AS lastOrderTime, " +
            "DATE_FORMAT(MIN(o.create_time), '%Y-%m-%d %H:%i:%s') AS firstOrderTime, " +
            "DATEDIFF(NOW(), MAX(o.create_time)) AS daysSinceLastOrder, " +
            "CASE " +
            "  WHEN COUNT(*) > 1 THEN ROUND(DATEDIFF(MAX(o.create_time), MIN(o.create_time)) / (COUNT(*) - 1), 1) " +
            "  ELSE NULL " +
            "END AS avgIntervalDays " +
            "FROM sale_order_main o " +
            "LEFT JOIN customer c ON c.id = o.customer_id AND c.del_flag = 0 " +
            "WHERE o.del_flag = 0 " +
            "AND o.order_status IN (2, 3, 5) " +
            "AND o.customer_id IS NOT NULL " +
            "GROUP BY o.customer_id, o.customer_name, c.contact_phone " +
            "ORDER BY totalSpent DESC " +
            "LIMIT #{limit}")
    List<MemberSummaryDTO> selectTopCustomers(@Param("limit") int limit);

    /**
     * 某月总销售额
     */
    @Select("SELECT COALESCE(SUM(pay_amount), 0) FROM sale_order_main " +
            "WHERE del_flag = 0 AND order_status IN (2, 3, 5) " +
            "AND DATE_FORMAT(create_time, '%Y-%m') = #{yearMonth}")
    BigDecimal selectMonthlyRevenue(@Param("yearMonth") String yearMonth);

    /**
     * 客户订单时间线（仅购买事件，触点由 TouchpointMapper 补充）
     */
    @Select("SELECT " +
            "DATE_FORMAT(o.create_time, '%Y-%m-%d %H:%i:%s') AS `time`, " +
            "'ORDER' AS eventType, " +
            "CONCAT('Order: ', o.pay_amount, ' ', IFNULL(o.currency_code, '')) AS summary, " +
            "o.pay_amount AS amount, " +
            "NULL AS productName, " +
            "'IN_STORE' AS channel " +
            "FROM sale_order_main o " +
            "WHERE o.del_flag = 0 " +
            "AND o.customer_id = #{customerId} " +
            "AND o.order_status IN (2, 3, 5) " +
            "ORDER BY o.create_time DESC " +
            "LIMIT 20")
    List<MemberJourneyDTO.TimelineItem> selectCustomerOrderTimeline(@Param("customerId") Long customerId);

    /**
     * 客户最近买的商品（用于召回话术："上次买的可乐快用完了吧？"）
     */
    @Select("SELECT soi.goods_name FROM sale_order_item soi " +
            "INNER JOIN sale_order_main som ON soi.order_id = som.id " +
            "WHERE som.del_flag = 0 AND som.customer_id = #{customerId} " +
            "AND som.order_status IN (2, 3, 5) " +
            "ORDER BY som.create_time DESC, soi.id DESC " +
            "LIMIT 1")
    String selectLastProductByCustomer(@Param("customerId") Long customerId);

    /**
     * 待跟进触点数
     */
    @Select("SELECT COUNT(*) FROM customer_touchpoint WHERE del_flag = 0 AND follow_up_done = 0")
    Integer selectPendingFollowUpCount();

    /**
     * 总客户数（有订单记录的）
     */
    @Select("SELECT COUNT(DISTINCT customer_id) FROM sale_order_main " +
            "WHERE del_flag = 0 AND order_status IN (2, 3, 5) AND customer_id IS NOT NULL")
    Integer selectTotalCustomerCount();

    /**
     * 所有客户的触点记录（用于按触点+意图匹配销售阶段）
     */
    @Select("SELECT customer_id, touchpoint_type, intent " +
            "FROM customer_touchpoint " +
            "WHERE del_flag = 0 AND customer_id IS NOT NULL " +
            "ORDER BY customer_id, contact_time DESC")
    List<com.psi.customer.dto.CustomerTouchpointRow> selectAllTouchpoints();
}
