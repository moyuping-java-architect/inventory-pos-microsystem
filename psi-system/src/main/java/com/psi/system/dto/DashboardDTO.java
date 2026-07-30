package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 仪表盘数据 DTO
 */
@Data
public class DashboardDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 今日采购金额
     */
    private BigDecimal todayPurchaseAmount;

    /**
     * 今日销售金额
     */
    private BigDecimal todaySaleAmount;

    /**
     * 库存 SKU 数量
     */
    private Long stockSkuCount;

    /**
     * 本月利润（粗略 = 本月销售 - 本月采购）
     */
    private BigDecimal monthProfit;

    /**
     * 销售趋势
     */
    private List<SaleTrendDTO> saleTrend;

    /**
     * 库存预警
     */
    private List<StockAlertDTO> stockAlert;

    /**
     * 最近单据
     */
    private List<RecentOrderDTO> recentOrders;

    @Data
    public static class SaleTrendDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private String date;
        private BigDecimal amount;
        private Integer orderCount;
    }

    @Data
    public static class StockAlertDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private String code;
        private BigDecimal stock;
        private BigDecimal minStock;
    }

    @Data
    public static class RecentOrderDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private String orderNo;
        private String type;
        private String customer;
        private BigDecimal amount;
        private String status;
        private String createTime;
    }

}
