package com.psi.common.feign;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 单据 Feign 响应（跨服务传输用）
 */
@Data
public class DocFeignResponse implements Serializable {

    private Long id;
    private String docNo;
    private String docType;
    private String docName;
    private String docTypeDesc;
    private Integer status;
    private String creatorId;
    private String creatorName;
    private String partnerId;
    private String partnerCode;
    private String partnerName;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private String orderNo;
    private Integer saleType;
    private Integer paymentType;
    private String currencyCode;
    private BigDecimal exchangeRate;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private Integer itemCount;
    private String remark;
    private String extJson;
    private String docDate;
    private String deliveryDate;
    private List<DocFeignItemResponse> items;

    @Data
    public static class DocFeignItemResponse implements Serializable {
        private Long id;
        private Long goodsId;
        private String goodsCode;
        private String skuCode;
        private String skuName;
        private String goodsName;
        private String goodsSpec;
        private String goodsUnit;
        private BigDecimal conversionRate;
        private BigDecimal unitPrice;
        private BigDecimal quantity;
        private BigDecimal amount;
        private BigDecimal taxRate;
        private BigDecimal taxAmount;
        private BigDecimal discountRate;
        private BigDecimal discountAmount;
        private String batchNo;
        private String expiryDate;
        private String remark;
    }
}