package com.psi.stock.dto;

import lombok.Data;

import java.util.List;

@Data
public class StockBatchOperateDTO {

    private List<StockBatchOperateItemDTO> items;
    private String sourceNo;
    private String sourceType;
    private String remark;
}
