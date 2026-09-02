package com.psi.sale.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PaymentAllocationDTO implements Serializable {

    private Long debtDetailId;
    private String billNo;
    private BigDecimal allocateAmount;
}