package com.psi.order.dto.biz.purchase;

import com.psi.order.entity.DocEntity;
import com.psi.order.entity.DocItemEntity;
import com.psi.order.dto.DocResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 采购订单DTO
 * 参照 purchase_order_main / purchase_order_item 正式表结构
 */
@Data
@Schema(description = "采购订单DTO")
public class PurchaseOrderDTO {

    @Schema(description = "草稿ID")
    private Long id;

    @Schema(description = "单据编号")
    private String docNo;

    @Schema(description = "商铺编码")
    private String shopCode;

    @Schema(description = "商铺名称")
    private String shopName;

    @Schema(description = "供应商ID")
    private String supplierId;

    @Schema(description = "供应商编码")
    private String supplierCode;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "部门ID")
    private String deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "仓库ID")
    private Long warehouseId;

    @Schema(description = "仓库编码")
    private String warehouseCode;

    @Schema(description = "仓库名称")
    private String warehouseName;

    @Schema(description = "订单日期")
    private LocalDateTime docDate;

    @Schema(description = "预计交货日期")
    private LocalDateTime deliveryDate;

    @Schema(description = "付款方式：1-预付定金 2-货到付款 3-月结")
    private Integer paymentType;

    @Schema(description = "货币编码")
    private String currencyCode;

    @Schema(description = "汇率")
    private BigDecimal exchangeRate;

    @Schema(description = "订单总金额（不含税）")
    private BigDecimal totalAmount;

    @Schema(description = "税额")
    private BigDecimal taxAmount;

    @Schema(description = "折扣金额")
    private BigDecimal discountAmount;

    @Schema(description = "实际付款金额")
    private BigDecimal payAmount;

    @Schema(description = "单据状态")
    private Integer status;

    @Schema(description = "审核状态：0-未审核 1-已审核 2-审核驳回")
    private Integer auditStatus;

    @Schema(description = "审核人ID")
    private Long auditBy;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人")
    private String creatorName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "明细列表")
    private List<Item> items;

    @Data
    @Schema(description = "采购订单明细")
    public static class Item {

        @Schema(description = "明细ID")
        private Long id;

        @Schema(description = "商品ID")
        private Long goodsId;

        @Schema(description = "商品编码")
        private String goodsCode;

        @Schema(description = "商品名称")
        private String goodsName;

        @Schema(description = "商品规格")
        private String goodsSpec;

        @Schema(description = "计量单位编码")
        private String unitCode;

        @Schema(description = "数量")
        private BigDecimal quantity;

        @Schema(description = "单价（不含税）")
        private BigDecimal unitPrice;

        @Schema(description = "金额（不含税）")
        private BigDecimal amount;

        @Schema(description = "税率(%)")
        private BigDecimal taxRate;

        @Schema(description = "税额")
        private BigDecimal taxAmount;

        @Schema(description = "折扣率(%)")
        private BigDecimal discountRate;

        @Schema(description = "折扣金额")
        private BigDecimal discountAmount;

        @Schema(description = "净金额（含税）")
        private BigDecimal netAmount;

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "行号")
        private Integer lineNo;

        public static Item fromEntity(DocItemEntity entity) {
            Item item = new Item();
            item.setId(entity.getId());
            item.setGoodsId(entity.getGoodsId());
            item.setGoodsCode(entity.getGoodsCode());
            item.setGoodsName(entity.getGoodsName());
            item.setGoodsSpec(entity.getGoodsSpec());
            item.setUnitCode(entity.getUnitCode());
            item.setQuantity(entity.getQuantity());
            item.setUnitPrice(entity.getUnitPrice());
            item.setAmount(entity.getAmount());
            item.setTaxRate(entity.getTaxRate());
            item.setTaxAmount(entity.getTaxAmount());
            item.setDiscountRate(entity.getDiscountRate());
            item.setDiscountAmount(entity.getDiscountAmount());
            item.setNetAmount(entity.getNetAmount());
            item.setRemark(entity.getRemark());
            item.setLineNo(entity.getLineNo());
            return item;
        }

        public static Item fromResponse(DocResponse.DocItemResponse itemResponse) {
            Item item = new Item();
            item.setId(itemResponse.getId());
            item.setGoodsId(itemResponse.getGoodsId());
            item.setGoodsCode(itemResponse.getGoodsCode());
            item.setGoodsName(itemResponse.getGoodsName());
            item.setGoodsSpec(itemResponse.getGoodsSpec());
            item.setUnitCode(itemResponse.getUnitCode());
            item.setQuantity(itemResponse.getQuantity());
            item.setUnitPrice(itemResponse.getUnitPrice());
            item.setAmount(itemResponse.getAmount());
            item.setTaxRate(itemResponse.getTaxRate());
            item.setTaxAmount(itemResponse.getTaxAmount());
            item.setDiscountRate(itemResponse.getDiscountRate());
            item.setDiscountAmount(itemResponse.getDiscountAmount());
            item.setNetAmount(itemResponse.getNetAmount());
            item.setRemark(itemResponse.getRemark());
            item.setLineNo(itemResponse.getLineNo());
            return item;
        }
    }

    /**
     * 从草稿实体转换
     */
    public static PurchaseOrderDTO fromEntity(DocEntity entity, List<DocItemEntity> itemEntities) {
        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setId(entity.getId());
        dto.setDocNo(entity.getDocNo());
        dto.setShopCode(entity.getShopCode());
        dto.setShopName(entity.getShopName());
        dto.setSupplierId(entity.getPartnerId());
        dto.setSupplierCode(entity.getPartnerCode());
        dto.setSupplierName(entity.getPartnerName());
        dto.setDeptId(entity.getDeptId());
        dto.setDeptName(entity.getDeptName());
        dto.setWarehouseId(entity.getWarehouseId());
        dto.setWarehouseCode(entity.getWarehouseCode());
        dto.setWarehouseName(entity.getWarehouseName());
        dto.setDocDate(entity.getDocDate());
        dto.setDeliveryDate(entity.getDeliveryDate());
        dto.setPaymentType(entity.getPaymentType());
        dto.setCurrencyCode(entity.getCurrencyCode());
        dto.setExchangeRate(entity.getExchangeRate());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setTaxAmount(entity.getTaxAmount());
        dto.setDiscountAmount(entity.getDiscountAmount());
        dto.setPayAmount(entity.getPayAmount());
        dto.setStatus(entity.getStatus());
        dto.setAuditStatus(entity.getAuditStatus());
        dto.setAuditBy(entity.getAuditBy());
        dto.setRemark(entity.getRemark());
        dto.setCreatorName(entity.getCreatorName());
        dto.setCreateTime(entity.getCreateTime());
        if (itemEntities != null) {
            dto.setItems(itemEntities.stream().map(Item::fromEntity).collect(Collectors.toList()));
        }
        return dto;
    }

    public static PurchaseOrderDTO fromResponse(DocResponse response) {
        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setId(response.getId());
        dto.setDocNo(response.getDocNo());
        dto.setShopCode(response.getShopCode());
        dto.setShopName(response.getShopName());
        dto.setSupplierId(response.getPartnerId());
        dto.setSupplierCode(response.getPartnerCode());
        dto.setSupplierName(response.getPartnerName());
        dto.setDeptId(response.getDeptId());
        dto.setDeptName(response.getDeptName());
        dto.setWarehouseId(response.getWarehouseId());
        dto.setWarehouseCode(response.getWarehouseCode());
        dto.setWarehouseName(response.getWarehouseName());
        dto.setDocDate(response.getDocDate());
        dto.setDeliveryDate(response.getDeliveryDate());
        dto.setPaymentType(response.getPaymentType());
        dto.setCurrencyCode(response.getCurrencyCode());
        dto.setExchangeRate(response.getExchangeRate());
        dto.setTotalAmount(response.getTotalAmount());
        dto.setTaxAmount(response.getTaxAmount());
        dto.setDiscountAmount(response.getDiscountAmount());
        dto.setPayAmount(response.getPayAmount());
        dto.setStatus(response.getStatus());
        dto.setAuditStatus(response.getAuditStatus());
        dto.setAuditBy(response.getAuditBy());
        dto.setRemark(response.getRemark());
        dto.setCreatorName(response.getCreatorName());
        dto.setCreateTime(response.getCreateTime());
        if (response.getItems() != null) {
            dto.setItems(response.getItems().stream().map(Item::fromResponse).collect(Collectors.toList()));
        }
        return dto;
    }
}