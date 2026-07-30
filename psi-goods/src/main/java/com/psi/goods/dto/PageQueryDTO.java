package com.psi.goods.dto;

import lombok.Data;

/**
 * 通用分页查询DTO
 */
@Data
public class PageQueryDTO {

    /**
     * 页码，默认1
     */
    private Integer pageNum = 1;

    /**
     * 每页数量，默认20
     */
    private Integer pageSize = 20;

    /**
     * 排序字段
     */
    private String sortBy;

    /**
     * 排序方向（asc/desc）
     */
    private String sortDir = "desc";
}