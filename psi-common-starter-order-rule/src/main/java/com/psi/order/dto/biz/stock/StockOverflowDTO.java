package com.psi.order.dto.biz.stock;

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
 * 报溢单DTO
 * 参照 stock_overflow_main / stock_overflow_item 正式表结构
 */
@Data
@Schema(description = "报溢单DTO")
public class StockOverflowDTO {

    @Schema(description = "草稿ID")
    private Long id;

    @Schema(description = "报溢单编号")
    private String docNo;

    @Schema(description = "商铺编码")
    private String shopCode;

    @Schema(description = "商铺名称")
    private String shopName;

    @Schema(description = "仓库ID")
    private Long warehouseId;

    @Schema(description = "仓库编码")
    private String warehouseCode;

    @Schema(description = "仓库名称")
    private String warehouseName;

    @Schema(description = "报溢日期")
    private LocalDateTime docDate;

    @Schema(description = "报溢总金额")
    private BigDecimal totalAmount;

    @Schema(description = "税额")
    private BigDecimal taxAmount;

    @Schema(description = "单据状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人")
    private String creatorName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "明细列表")
    private List<Item> items;

    @Data
    @Schema(description = "报溢明细")
    public static class Item {

        @Schema(description = "明细ID")
        private Long id;

        @Schema(description = "商品编码")
        private String goodsCode;

        @Schema(description = "商品名称")
        private String goodsName;

        @Schema(description = "商品规格")
        private String goodsSpec;

        @Schema(description = "单位")
        private String goodsUnit;

        @Schema(description = "报溢数量")
        private BigDecimal quantity;

        @Schema(description = "单价")
        private BigDecimal unitPrice;

        @Schema(description = "金额")
        private BigDecimal amount;

        @Schema(description = "税额")
        private BigDecimal taxAmount;

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "行号")
        private Integer lineNo;

        public static Item fromEntity(DocItemEntity entity) {
            Item item = new Item();
            item.setId(entity.getId());
            item.setGoodsCode(entity.getGoodsCode());
            item.setGoodsName(entity.getGoodsName());
            item.setGoodsSpec(entity.getGoodsSpec());
            item.setGoodsUnit(entity.getGoodsUnit());
            item.setQuantity(entity.getQuantity());
            item.setUnitPrice(entity.getUnitPrice());
            item.setAmount(entity.getAmount());
            item.setTaxAmount(entity.getTaxAmount());
            item.setRemark(entity.getRemark());
            item.setLineNo(entity.getLineNo());
            return item;
        }

        public static Item fromResponse(DocResponse.DocItemResponse itemResponse) {
            Item item = new Item();
            item.setId(itemResponse.getId());
            item.setGoodsCode(itemResponse.getGoodsCode());
            item.setGoodsName(itemResponse.getGoodsName());
            item.setGoodsSpec(itemResponse.getGoodsSpec());
            item.setGoodsUnit(itemResponse.getGoodsUnit());
            item.setQuantity(itemResponse.getQuantity());
            item.setUnitPrice(itemResponse.getUnitPrice());
            item.setAmount(itemResponse.getAmount());
            item.setTaxAmount(itemResponse.getTaxAmount());
            item.setRemark(itemResponse.getRemark());
            item.setLineNo(itemResponse.getLineNo());
            return item;
        }
    }

    public static StockOverflowDTO fromEntity(DocEntity entity, List<DocItemEntity> itemEntities) {
        StockOverflowDTO dto = new StockOverflowDTO();
        dto.setId(entity.getId());
        dto.setDocNo(entity.getDocNo());
        dto.setShopCode(entity.getShopCode());
        dto.setShopName(entity.getShopName());
        dto.setWarehouseId(entity.getWarehouseId());
        dto.setWarehouseCode(entity.getWarehouseCode());
        dto.setWarehouseName(entity.getWarehouseName());
        dto.setDocDate(entity.getDocDate());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setTaxAmount(entity.getTaxAmount());
        dto.setStatus(entity.getStatus());
        dto.setRemark(entity.getRemark());
        dto.setCreatorName(entity.getCreatorName());
        dto.setCreateTime(entity.getCreateTime());
        if (itemEntities != null) {
            dto.setItems(itemEntities.stream().map(Item::fromEntity).collect(Collectors.toList()));
        }
        return dto;
    }

    public static StockOverflowDTO fromResponse(DocResponse response) {
        StockOverflowDTO dto = new StockOverflowDTO();
        dto.setId(response.getId());
        dto.setDocNo(response.getDocNo());
        dto.setShopCode(response.getShopCode());
        dto.setShopName(response.getShopName());
        dto.setWarehouseId(response.getWarehouseId());
        dto.setWarehouseCode(response.getWarehouseCode());
        dto.setWarehouseName(response.getWarehouseName());
        dto.setDocDate(response.getDocDate());
        dto.setTotalAmount(response.getTotalAmount());
        dto.setTaxAmount(response.getTaxAmount());
        dto.setStatus(response.getStatus());
        dto.setRemark(response.getRemark());
        dto.setCreatorName(response.getCreatorName());
        dto.setCreateTime(response.getCreateTime());
        if (response.getItems() != null) {
            dto.setItems(response.getItems().stream().map(Item::fromResponse).collect(Collectors.toList()));
        }
        return dto;
    }
}