package com.psi.cashier.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 小票打印数据传输对象
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
public class ReceiptPrintDTO {

    private String orderNo;

    private String shopName;

    private String shopCode;

    private String posName;

    private String cashierName;

    private String createTime;

    private Integer bizType;

    private String bizTypeName;

    private List<ReceiptItemDTO> items;

    private BigDecimal totalAmount;

    private BigDecimal realAmount;

    private BigDecimal discountAmount;

    private Integer payStatus;

    private String payStatusName;

    private List<ReceiptPayDTO> pays;

    private String memberName;

    private String memberCardNo;

    private BigDecimal memberBalance;

    private String footer;
}
