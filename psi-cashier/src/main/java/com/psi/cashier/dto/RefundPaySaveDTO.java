package com.psi.cashier.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 退款支付保存DTO
 * 用于退款支付的保存请求
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
public class RefundPaySaveDTO {

    private Integer payId;

    private String payName;

    private BigDecimal refundAmount;

    private String currency;
}