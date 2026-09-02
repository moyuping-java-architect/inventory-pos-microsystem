package com.psi.sale.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PromotionCalculateDTO {

    private String customerLevel;

    private List<PromotionCalculateItemDTO> items;

    private BigDecimal totalAmount;

    private BigDecimal totalQuantity;

    private String warehouseCode;

    @Data
    public static class PromotionCalculateItemDTO {
        private String skuCode;
        private String categoryCode;
        private BigDecimal quantity;
        private BigDecimal price;
        private BigDecimal amount;
    }
}
