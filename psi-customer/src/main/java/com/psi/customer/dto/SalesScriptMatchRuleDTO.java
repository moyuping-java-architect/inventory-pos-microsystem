package com.psi.customer.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 话术匹配规则 DTO
 */
@Data
public class SalesScriptMatchRuleDTO {

    private Long id;
    private String tenantId;
    private String ruleName;
    private Integer stageEnabled;
    private BigDecimal stageWeight;
    private Integer tagEnabled;
    private BigDecimal tagWeight;
    private Integer timeEnabled;
    private BigDecimal timeWeight;
    private Integer priorityEnabled;
    private BigDecimal priorityWeight;
}
