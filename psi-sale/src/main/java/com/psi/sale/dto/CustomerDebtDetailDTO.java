package com.psi.sale.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CustomerDebtDetailDTO implements Serializable {

    private Long id;
    private Long customerId;
    private String customerCode;
    private String customerName;
    private Integer billType;
    private String billNo;
    private BigDecimal debtAmount;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private String debtDate;
    private String dueDate;
    private Integer status;
    private String remark;
    private Integer delFlag;
    private LocalDateTime createTime;
}