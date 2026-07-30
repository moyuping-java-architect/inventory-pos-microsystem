package com.psi.stock.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.psi.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_inventory_init_main")
public class InventoryInitMainEntity extends BaseEntity {

    private String initNo;
    private String docName;
    private String warehouseCode;
    private String warehouseName;
    private String initDate;
    private BigDecimal totalAmount;
    private String remark;

    private Integer orderStatus;
}