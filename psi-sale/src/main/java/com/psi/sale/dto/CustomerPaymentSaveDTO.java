package com.psi.sale.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CustomerPaymentSaveDTO implements Serializable {

    private Long customerId;
    private String customerCode;
    private String customerName;
    private String paymentDate;
    private BigDecimal paymentAmount;
    private Integer paymentMethod;
    private String accountNo;
    private String bankName;
    private String remark;
    private List<PaymentAllocationDTO> allocations;
}