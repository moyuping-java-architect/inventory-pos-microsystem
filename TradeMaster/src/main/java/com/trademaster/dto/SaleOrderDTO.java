package com.trademaster.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SaleOrderDTO {
    private Long customerId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal actualAmount;
    private String paymentType;
    private Long cashierId;
    private String remark;
    private List<SaleOrderItemDTO> items;
}
