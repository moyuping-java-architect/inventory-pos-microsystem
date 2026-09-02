package com.psi.cashier.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 挂单保存DTO
 * 用于挂单的保存请求
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
public class PendingMainSaveDTO {

    private String pendingNo;

    private String tenantId;

    private String shopCode;

    private String posId;

    private Integer operatorId;

    private String pendingName;

    private BigDecimal totalAmount;

    private List<PendingItemSaveDTO> items;
}