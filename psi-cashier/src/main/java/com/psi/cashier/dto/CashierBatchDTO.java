package com.psi.cashier.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 收银端批次展示 DTO
 */
@Data
public class CashierBatchDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 生产日期
     */
    private String productionDate;

    /**
     * 有效期至
     */
    private String expireDate;

    /**
     * 可用数量
     */
    private BigDecimal availableQuantity;
}
