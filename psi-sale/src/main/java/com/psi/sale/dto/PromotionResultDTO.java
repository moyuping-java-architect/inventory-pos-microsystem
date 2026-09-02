package com.psi.sale.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PromotionResultDTO {

    private BigDecimal discountAmount;

    private BigDecimal finalAmount;

    private List<PromotionAppliedDTO> appliedPromotions;

    private List<PromotionItemResultDTO> itemResults;

    @Data
    public static class PromotionAppliedDTO {
        private Long promotionId;
        private String promotionNo;
        private String promotionName;
        private Integer promotionType;
        private BigDecimal discountAmount;
    }

    @Data
    public static class PromotionItemResultDTO {
        private String skuCode;
        private BigDecimal originalPrice;
        private BigDecimal finalPrice;
        private BigDecimal discountAmount;
    }
}
