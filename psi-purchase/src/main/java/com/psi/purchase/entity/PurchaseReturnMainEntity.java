package com.psi.purchase.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_return_main")
public class PurchaseReturnMainEntity extends BaseEntity {

    /**
     * 退货单编号
     */
    private String returnNo;

    /**
     * 单据名称（必填，默认：单据类型+当天日期）
     */
    private String docName;

    /**
     * 关联入库单编号
     */
    private String inNo;

    /**
     * 关联采购订单编号
     */
    private String orderNo;

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
     * 退货状态：1-待审核 2-已审核 3-已取消
     */
    private Integer returnStatus;

    /**
     * 退货原因
     */
    private String returnReason;

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