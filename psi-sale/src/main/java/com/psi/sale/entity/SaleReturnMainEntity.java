package com.psi.sale.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sale_return_main")
public class SaleReturnMainEntity extends BaseEntity {

    /**
     * 销售退货单编号
     */
    private String returnNo;

    /**
     * 单据名称（必填，默认：单据类型+当天日期）
     */
    private String docName;

    /**
     * 关联出库单编号
     */
    private String outNo;

    /**
     * 关联销售订单编号
     */
    private String orderNo;

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
     * 退货日期
     */
    private String returnDate;

    /**
     * 仓库编码
     */
    private String warehouseCode;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 退货总金额（不含税）
     */
    private BigDecimal totalAmount;

    /**
     * 税额
     */
    private BigDecimal taxAmount;

    /**
     * 实际退款金额
     */
    private BigDecimal payAmount;

    /**
     * 退货原因
     */
    private String returnReason;

    /**
     * 备注
     */
    private String remark;

    /**
     * 订单状态：1-待审批 2-审核通过 3-已入库 4-已取消 5-已完成
     */
    private Integer orderStatus;
}