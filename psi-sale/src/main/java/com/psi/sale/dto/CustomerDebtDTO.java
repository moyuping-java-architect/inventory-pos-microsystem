package com.psi.sale.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CustomerDebtDTO implements Serializable {

    private Long id;
    private Long customerId;
    private String customerCode;
    private String customerName;
    private BigDecimal totalDebtAmount;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private BigDecimal creditLimit;
    private BigDecimal availableCredit;
    private String remark;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}