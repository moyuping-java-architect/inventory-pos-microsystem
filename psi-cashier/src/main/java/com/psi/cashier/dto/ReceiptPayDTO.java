package com.psi.cashier.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 小票支付信息数据传输对象
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
public class ReceiptPayDTO {

    private String payName;

    private BigDecimal payAmount;

    private String payTime;
}
