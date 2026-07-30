package com.psi.sale.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PromotionQueryDTO implements Serializable {

    private String promotionNo;

    private String promotionName;

    private Integer promotionType;

    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
