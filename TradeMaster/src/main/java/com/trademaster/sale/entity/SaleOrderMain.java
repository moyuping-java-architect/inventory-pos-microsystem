package com.trademaster.sale.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sale_order_main")
public class SaleOrderMain {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private String docName;

    private String customerCode;

    private String customerName;

    private Integer paymentType;

    private String currencyCode;

    private BigDecimal exchangeRate;

    private BigDecimal totalAmount;

    private BigDecimal taxAmount;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;

    private BigDecimal paidAmount;

    private LocalDateTime orderDate;

    private LocalDateTime deliveryDate;

    private Integer status;

    private Long auditBy;

    private LocalDateTime auditTime;

    private String remark;

    @TableLogic
    private Integer delFlag;

    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
