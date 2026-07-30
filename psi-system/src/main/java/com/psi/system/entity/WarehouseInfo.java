package com.psi.system.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("warehouse_info")
public class WarehouseInfo extends BaseEntity {

    private Long shopId;

    private String warehouseName;

    private String warehouseCode;

    private String address;

    private BigDecimal capacity;

    private String manager;

    private String phone;
}