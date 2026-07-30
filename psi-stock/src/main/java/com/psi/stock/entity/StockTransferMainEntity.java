package com.psi.stock.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.psi.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_transfer_main")
public class StockTransferMainEntity extends BaseEntity {

    private String transferNo;

    @TableField("doc_name")
    private String docName;

    @TableField("from_warehouse_code")
    private String fromWarehouseCode;

    @TableField("from_warehouse_name")
    private String fromWarehouseName;

    @TableField("to_warehouse_code")
    private String toWarehouseCode;

    @TableField("to_warehouse_name")
    private String toWarehouseName;

    @TableField("transfer_date")
    private String transferDate;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("tax_amount")
    private BigDecimal taxAmount;

    private String remark;

    @TableField("status")
    private Integer status;
}
