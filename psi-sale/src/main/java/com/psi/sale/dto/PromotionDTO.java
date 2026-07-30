package com.psi.sale.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PromotionDTO {

    private Long id;

    private String promotionNo;

    private String promotionName;

    private Integer promotionType;

    private Integer discountType;

    private BigDecimal discountValue;

    private BigDecimal minAmount;

    private BigDecimal minQuantity;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer scopeType;

    private Integer status;

    private Integer priority;

    private Integer superimposable;

    private String remark;

    private List<PromotionItemDTO> items;
}
