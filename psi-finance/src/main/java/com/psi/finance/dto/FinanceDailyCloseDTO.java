package com.psi.finance.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FinanceDailyCloseDTO {
    private Long id;
    private String storeCode;
    private String storeName;
    private String closeDate;
    private BigDecimal saleAmount;
    private BigDecimal costAmount;
    private BigDecimal profitAmount;
    private BigDecimal cashIn;
    private BigDecimal cashOut;
    private BigDecimal transferIn;
    private BigDecimal transferOut;
    private BigDecimal receivableAmount;
    private BigDecimal payableAmount;
    private BigDecimal cashBalance;
    private BigDecimal wechatBalance;
    private BigDecimal alipayBalance;
    private BigDecimal bankBalance;
    private BigDecimal totalBalance;
    private String closeBy;
    private String closeTime;
    private Integer closeStatus;
    private String closeStatusName;
    private String remark;
    private LocalDateTime createTime;
}