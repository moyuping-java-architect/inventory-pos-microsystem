package com.trademaster.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.trademaster.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sale_order")
public class SaleOrder extends BaseEntity {
    private String orderNo;
    private Long customerId;
    private java.math.BigDecimal totalAmount;
    private java.math.BigDecimal discountAmount;
    private java.math.BigDecimal actualAmount;
    private String paymentType;
    private String status;
    private String remark;
    private Long cashierId;
}
