package com.psi.customer.dto;

import lombok.Data;

/**
 * 客户标签 DTO
 */
@Data
public class CustomerTagDTO {

    private Long id;
    private String tenantId;
    private String tagCode;
    private String tagName;
    private String category;
    private String color;
    private Integer sort;
}
