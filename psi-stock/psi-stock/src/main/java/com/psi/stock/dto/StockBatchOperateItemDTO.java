package com.psi.stock.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存批量操作明细项
 */
@Data
public class StockBatchOperateItemDTO {

    /** 仓库编码 */
    private String warehouseCode;

    /** SKU 编码 */
    private String skuCode;

    /** 操作数量 */
    private BigDecimal quantity;
}
