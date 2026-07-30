package com.psi.sale.dto;

import lombok.Data;

@Data
public class PromotionItemDTO {

    private Long id;

    private Long promotionId;

    private String promotionNo;

    private Integer itemType;

    private String itemCode;

    private String itemName;

    private String categoryCode;

    private String categoryName;
}
