package com.psi.sale.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sale_order_main")
public class SaleOrderMainEntity extends BaseEntity {

    /**
     * 销售订单编号
     */
    private String orderNo;

    /**
     * 单据名称（必填，默认：单据类型+当天日期）
     */
    private String docName;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 客户编码
     */
    private String customerCode;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 订单日期
     */
    private String orderDate;

    /**
     * 交货日期
     */
    private String deliveryDate;

    /**
     * 销售类型：1-普通销售 2-批发 3-零售
     */
    private Integer saleType;

    /**
     * 付款方式：1-现金 2-刷卡 3-赊销
     */
    private Integer paymentType;

    /**
     * 货币编码
     */
    private String currencyCode;

    /**
     * 汇率
     */
    private BigDecimal exchangeRate;

    /**
     * 订单总金额（不含税）
     */
    private BigDecimal totalAmount;

    /**
     * 税额
     */
    private BigDecimal taxAmount;

    /**
     * 折扣金额
     */
    private BigDecimal discountAmount;

    /**
     * 实际收款金额
     */
    private BigDecimal payAmount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 仓库编码
     */
    private String warehouseCode;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 订单状态：1-待审批 2-审核通过 3-已入库 4-已取消 5-已完成
     */
    private Integer orderStatus;
}