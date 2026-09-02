package com.psi.customer.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 历史销售订单行，用于存量数据回溯。
 * <p>
 * 系统上线前的老订单不会经过事件总线，靠这张查询结果把它们合成
 * SALE.ORDER_APPROVED 事件重新喂给引擎，补出历史触点和旅程状态。
 *
 * @author PSI
 */
@Data
public class SaleOrderEventRow {

    /** 客户ID */
    private Long customerId;

    /** 单据号，作为幂等键 */
    private String docNo;

    /** 订单金额 */
    private BigDecimal orderAmount;

    /** 币种 */
    private String currency;

    /** 下单时间，回溯时作为事件发生时间 */
    private String occurTime;
}
