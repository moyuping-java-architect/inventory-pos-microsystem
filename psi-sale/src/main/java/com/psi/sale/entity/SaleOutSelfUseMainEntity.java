package com.psi.sale.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sale_out_self_use_main")
public class SaleOutSelfUseMainEntity extends BaseEntity {

    /**
     * 出库单编号
     */
    private String outNo;

    /**
     * 单据名称
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
     * 出库日期
     */
    private String outDate;

    /**
     * 仓库编码
     */
    private String warehouseCode;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 出库总金额（不含税）
     */
    private BigDecimal totalAmount;

    /**
     * 税额
     */
    private BigDecimal taxAmount;

    /**
     * 自用原因
     */
    private String selfUseReason;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态：1-待审批 2-审核通过 3-已完成 4-已取消
     */
    private Integer status;

    /**
     * 明细列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<SaleOutSelfUseItemEntity> items;
}
