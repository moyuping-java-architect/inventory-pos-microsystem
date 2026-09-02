package com.psi.cashier.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 退货单保存DTO
 * 用于退货订单的保存请求
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
public class RefundMainSaveDTO {

    private String refundNo;

    private String tenantId;

    private String shopCode;

    private String posId;

    private String sourceOrderNo;

    private Integer operatorId;

    private BigDecimal totalRefund;

    private BigDecimal netRefund;

    private BigDecimal taxRefund;

    private String currency;

    private BigDecimal exchangeRate;

    private BigDecimal originalRefund;

    private Integer refundType;

    private String remark;

    private List<RefundItemSaveDTO> items;

    private List<RefundPaySaveDTO> pays;
}