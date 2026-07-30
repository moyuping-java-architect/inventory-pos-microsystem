package com.psi.sale.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CustomerPaymentDTO implements Serializable {

    private Long id;
    private String paymentNo;
    private Long customerId;
    private String customerCode;
    private String customerName;
    private String paymentDate;
    private BigDecimal paymentAmount;
    private Integer paymentMethod;
    private String accountNo;
    private String bankName;
    private String remark;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}