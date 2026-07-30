package com.psi.cashier.dto;

import lombok.Data;

/**
 * 日结创建DTO
 * 用于创建日结单的请求
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
public class SettlementCreateDTO {

    /**
     * 日结日期
     */
    private String settleDate;

    /**
     * 收银员ID（可选）
     */
    private Integer operatorId;
}