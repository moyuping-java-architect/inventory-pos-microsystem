package com.psi.stock.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StockLossMainDTO implements Serializable {

    private Long id;
    private String lossNo;
    private String docName;
    private String warehouseCode;
    private String warehouseName;
    private String lossDate;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private String remark;
    private Integer status;
    private Integer orderStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<StockLossItemDTO> items;
}