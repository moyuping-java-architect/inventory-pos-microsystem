package com.psi.member.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberLevelDTO {

    private Long id;

    private String levelName;

    private Integer level;

    private BigDecimal discount;

    private BigDecimal minConsume;

    private Integer minPoints;

    private BigDecimal pointRate;

    private String levelIcon;

    private String description;

    private Integer sortOrder;
}
