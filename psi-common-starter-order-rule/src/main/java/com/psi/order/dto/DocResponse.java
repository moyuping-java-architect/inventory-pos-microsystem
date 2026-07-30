package com.psi.order.dto;

import com.psi.order.entity.DocEntity;
import com.psi.order.entity.DocItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 单据响应DTO
 */
@Data
public class DocResponse {

    private Long id;
    private String docNo;
    private String docType;
    private String docName;
    private String docTypeDesc;
    private Integer status;
    private String statusDesc;
    private String shopCode;
    private String shopName;
    private String creatorId;
    private String creatorName;
    private String deptId;
    private String deptName;
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
    private LocalDateTime docDate;
    private LocalDateTime deliveryDate;
    private Integer auditStatus;
    private Long auditBy;
    private LocalDateTime approveTime;
    private LocalDateTime executeTime;
    private LocalDateTime completeTime;
    private LocalDateTime cancelTime;
    private String remark;
    private String extJson;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<DocItemResponse> items;

    public static DocResponse fromEntity(DocEntity entity) {
        DocResponse response = new DocResponse();
        response.setId(entity.getId());
        response.setDocNo(entity.getDocNo());
        response.setDocType(entity.getDocType());
        response.setDocName(entity.getDocName());
        response.setDocTypeDesc(entity.getDocTypeEnum().getDescription());
        response.setStatus(entity.getStatus());
        response.setStatusDesc(entity.getStatusEnum().getDescription());
        response.setShopCode(entity.getShopCode());
        response.setShopName(entity.getShopName());
        response.setCreatorId(entity.getCreatorId());
        response.setCreatorName(entity.getCreatorName());
        response.setDeptId(entity.getDeptId());
        response.setDeptName(entity.getDeptName());
        response.setPartnerId(entity.getPartnerId());
        response.setPartnerCode(entity.getPartnerCode());
        response.setPartnerName(entity.getPartnerName());
        response.setWarehouseId(entity.getWarehouseId());
        response.setWarehouseCode(entity.getWarehouseCode());
        response.setWarehouseName(entity.getWarehouseName());
        response.setOrderNo(entity.getOrderNo());
        response.setSaleType(entity.getSaleType());
        response.setPaymentType(entity.getPaymentType());
        response.setCurrencyCode(entity.getCurrencyCode());
        response.setExchangeRate(entity.getExchangeRate());
        response.setTotalAmount(entity.getTotalAmount());
        response.setTaxAmount(entity.getTaxAmount());
        response.setDiscountAmount(entity.getDiscountAmount());
        response.setPayAmount(entity.getPayAmount());
        response.setItemCount(entity.getItemCount());
        response.setDocDate(entity.getDocDate());
        response.setDeliveryDate(entity.getDeliveryDate());
        response.setAuditStatus(entity.getAuditStatus());
        response.setAuditBy(entity.getAuditBy());
        response.setApproveTime(entity.getApproveTime());
        response.setExecuteTime(entity.getExecuteTime());
        response.setCompleteTime(entity.getCompleteTime());
        response.setCancelTime(entity.getCancelTime());
        response.setRemark(entity.getRemark());
        response.setExtJson(entity.getExtJson());
        response.setCreateTime(entity.getCreateTime());
        response.setUpdateTime(entity.getUpdateTime());
        return response;
    }

    @Data
    public static class DocItemResponse {

        private Long id;
        private String shopCode;
        private String shopName;
        private Long goodsId;
        private String goodsCode;
        private String skuCode;
        private String skuName;
        private String barcode;
        private String goodsName;
        private String goodsSpec;
        private String unitCode;
        private String goodsUnit;
        private BigDecimal conversionRate;
        private BigDecimal unitPrice;
        private BigDecimal quantity;
        private BigDecimal amount;
        private BigDecimal taxRate;
        private BigDecimal taxAmount;
        private BigDecimal discountRate;
        private BigDecimal discountAmount;
        private BigDecimal netAmount;
        private BigDecimal costPrice;
        private BigDecimal costAmount;
        private BigDecimal payAmount;
        private Long stockId;
        private String batchNo;
        private String expiryDate;
        private String remark;
        private Integer lineNo;

        public static DocItemResponse fromEntity(DocItemEntity entity) {
            DocItemResponse response = new DocItemResponse();
            response.setId(entity.getId());
            response.setShopCode(entity.getShopCode());
            response.setShopName(entity.getShopName());
            response.setGoodsId(entity.getGoodsId());
            response.setGoodsCode(entity.getGoodsCode());
            response.setSkuCode(entity.getSkuCode());
            response.setSkuName(entity.getSkuName());
            response.setBarcode(entity.getBarcode());
            response.setGoodsName(entity.getGoodsName());
            response.setGoodsSpec(entity.getGoodsSpec());
            response.setUnitCode(entity.getUnitCode());
            response.setGoodsUnit(entity.getGoodsUnit());
            response.setConversionRate(entity.getConversionRate());
            response.setUnitPrice(entity.getUnitPrice());
            response.setQuantity(entity.getQuantity());
            response.setAmount(entity.getAmount());
            response.setTaxRate(entity.getTaxRate());
            response.setTaxAmount(entity.getTaxAmount());
            response.setDiscountRate(entity.getDiscountRate());
            response.setDiscountAmount(entity.getDiscountAmount());
            response.setNetAmount(entity.getNetAmount());
            response.setCostPrice(entity.getCostPrice());
            response.setCostAmount(entity.getCostAmount());
            response.setPayAmount(entity.getPayAmount());
            response.setStockId(entity.getStockId());
            response.setBatchNo(entity.getBatchNo());
            response.setExpiryDate(entity.getExpiryDate());
            response.setRemark(entity.getRemark());
            response.setLineNo(entity.getLineNo());
            return response;
        }
    }
}