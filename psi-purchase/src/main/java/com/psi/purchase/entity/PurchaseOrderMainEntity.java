package com.psi.purchase.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_order_main")
public class PurchaseOrderMainEntity extends BaseEntity {

    /**
     * 采购订单编号
     */
    private String orderNo;

    /**
     * 单据名称（必填，默认：单据类型+当天日期）
     */
    private String docName;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 供应商编码
     */
    private String supplierCode;

    /**
     * 供应商名称
     */
    private String supplierName;

    /**
     * 订单日期
     */
    private String orderDate;

    /**
     * 预计交货日期
     */
    private String deliveryDate;

    /**
     * 付款方式：1-预付定金 2-货到付款 3-月结
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
     * 实际付款金额
     */
    private BigDecimal payAmount;

    /**
     * 订单状态：1-待审核 2-已审核 3-已取消 4-已完成
     */
    private Integer orderStatus;

    /**
     * 备注
     */
    private String remark;

    /**
     * 审核状态：0-未审核 1-已审核 2-审核驳回
     */
    private Integer auditStatus;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核人ID
     */
    private Long auditBy;
}