package com.psi.stock.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.psi.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_over_main")
public class StockOverMainEntity extends BaseEntity {

    private String overNo;

    @TableField("doc_name")
    private String docName;

    @TableField("warehouse_code")
    private String warehouseCode;

    @TableField("warehouse_name")
    private String warehouseName;

    @TableField("over_date")
    private String overDate;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("tax_amount")
    private BigDecimal taxAmount;

    private String remark;

    @TableField("status")
    private Integer status;
}