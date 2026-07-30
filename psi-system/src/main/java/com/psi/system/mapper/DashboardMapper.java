package com.psi.system.mapper;

import com.psi.system.dto.DashboardDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 仪表盘数据 Mapper
 */
@Mapper
public interface DashboardMapper {

    /**
     * 今日采购金额
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM psi_purchase.purchase_in_main WHERE in_date = #{today} AND del_flag = 0 AND status = 1")
    BigDecimal selectTodayPurchaseAmount(@Param("today") String today);

    /**
     * 今日销售金额
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM psi_sale.sale_out_main WHERE out_date = #{today} AND del_flag = 0 AND status = 1")
    BigDecimal selectTodaySaleAmount(@Param("today") String today);

    /**
     * 本月采购金额
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM psi_purchase.purchase_in_main WHERE in_date LIKE CONCAT(#{month}, '%') AND del_flag = 0 AND status = 1")
    BigDecimal selectMonthPurchaseAmount(@Param("month") String month);

    /**
     * 本月销售金额
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM psi_sale.sale_out_main WHERE out_date LIKE CONCAT(#{month}, '%') AND del_flag = 0 AND status = 1")
    BigDecimal selectMonthSaleAmount(@Param("month") String month);

    /**
     * 库存 SKU 数量
     */
    @Select("SELECT COUNT(DISTINCT sku_code) FROM psi_stock.stock WHERE del_flag = 0")
    Long selectStockSkuCount();

    /**
     * 最近 7 天销售趋势
     */
    @Select("<script>" +
            "SELECT out_date AS date, COALESCE(SUM(total_amount), 0) AS amount, COUNT(*) AS orderCount " +
            "FROM psi_sale.sale_out_main WHERE out_date IN " +
            "<foreach item='d' index='index' collection='dates' open='(' separator=',' close=')'>#{d}</foreach> " +
            "AND del_flag = 0 AND status = 1 GROUP BY out_date ORDER BY out_date ASC" +
            "</script>")
    List<DashboardDTO.SaleTrendDTO> selectSaleTrend(@Param("dates") List<String> dates);

    /**
     * 库存预警：库存数量低于最低库存的 SKU
     */
    @Select("SELECT s.goods_name AS name, s.sku_code AS code, s.quantity AS stock, g.min_stock_qty AS minStock " +
            "FROM psi_stock.stock s LEFT JOIN psi_goods.goods_sku g ON s.sku_code = g.sku_code " +
            "WHERE s.del_flag = 0 AND g.min_stock_qty IS NOT NULL AND s.quantity < g.min_stock_qty " +
            "ORDER BY s.quantity ASC LIMIT 10")
    List<DashboardDTO.StockAlertDTO> selectStockAlert();

    /**
     * 最近单据（采购入库 + 销售出库）
     */
    @Select("SELECT orderNo, type, customer, amount, status, createTime FROM (" +
            "SELECT in_no AS orderNo, '采购' AS type, supplier_name AS customer, total_amount AS amount, '已入库' AS status, create_time AS createTime " +
            "FROM psi_purchase.purchase_in_main WHERE del_flag = 0 AND status = 1 " +
            "UNION ALL " +
            "SELECT out_no AS orderNo, '销售' AS type, customer_name AS customer, total_amount AS amount, '已出库' AS status, create_time AS createTime " +
            "FROM psi_sale.sale_out_main WHERE del_flag = 0 AND status = 1 " +
            ") t ORDER BY createTime DESC LIMIT 10")
    List<DashboardDTO.RecentOrderDTO> selectRecentOrders();
}
