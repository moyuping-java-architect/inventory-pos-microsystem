package com.psi.stock.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.psi.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_check_main")
public class StockCheckMainEntity extends BaseEntity {

    private String checkNo;

    @TableField("doc_name")
    private String docName;

    @TableField("warehouse_code")
    private String warehouseCode;

    @TableField("warehouse_name")
    private String warehouseName;

    @TableField("check_date")
    private String checkDate;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("diff_amount")
    private BigDecimal diffAmount;

    private String remark;

    @TableField("status")
    private Integer status;
}
