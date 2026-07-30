package com.trademaster.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.trademaster.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer")
public class Customer extends BaseEntity {
    private String customerName;
    private String phone;
    private String address;
    private java.math.BigDecimal totalSpent;
    private String memberLevel;
    private java.math.BigDecimal balance;
    private Integer points;
}
