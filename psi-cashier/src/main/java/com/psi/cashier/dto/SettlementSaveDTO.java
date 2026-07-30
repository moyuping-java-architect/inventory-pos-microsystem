package com.psi.cashier.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 日结保存DTO
 * 用于日结单的保存请求
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
public class SettlementSaveDTO {

    private String settleNo;

    private String tenantId;

    private String shopCode;

    private String posId;

    private Integer operatorId;

    private String username;

    private String realName;

    private String beginTime;

    private String endTime;

    private Integer totalOrder;

    private BigDecimal totalAmount;

    private BigDecimal totalReal;

    private BigDecimal totalDiscount;

    private BigDecimal cashAmount;

    private BigDecimal wechatAmount;

    private BigDecimal alipayAmount;

    private BigDecimal memberAmount;

    private BigDecimal otherAmount;
}