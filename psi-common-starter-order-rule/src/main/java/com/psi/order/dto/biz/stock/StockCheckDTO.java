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
 * 盘点单DTO
 * 参照 stock_check_main / stock_check_item 正式表结构
 */
@Data
@Schema(description = "盘点单DTO")
public class StockCheckDTO {

    @Schema(description = "草稿ID")
    private Long id;

    @Schema(description = "盘点单编号")
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

    @Schema(description = "盘点日期")
    private LocalDateTime docDate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "单据状态")
    private Integer status;

    @Schema(description = "创建人")
    private String creatorName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "明细列表")
    private List<Item> items;

    @Data
    @Schema(description = "盘点明细")
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

        @Schema(description = "账面数量")
        private BigDecimal bookQuantity;

        @Schema(description = "实际数量")
        private BigDecimal actualQuantity;

        @Schema(description = "差异数量")
        private BigDecimal diffQuantity;

        @Schema(description = "单价")
        private BigDecimal unitPrice;

        @Schema(description = "账面金额")
        private BigDecimal bookAmount;

        @Schema(description = "实际金额")
        private BigDecimal actualAmount;

        @Schema(description = "差异金额")
        private BigDecimal diffAmount;

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "行号")
        private Integer lineNo;

        /**
         * 从草稿明细转换（盘点单特殊：用ext_json字段传递盘点特有字段）
         */
        public static Item fromEntity(DocItemEntity entity) {
            Item item = new Item();
            item.setId(entity.getId());
            item.setGoodsCode(entity.getGoodsCode());
            item.setGoodsName(entity.getGoodsName());
            item.setGoodsSpec(entity.getGoodsSpec());
            item.setGoodsUnit(entity.getGoodsUnit());
            // 盘点单使用quantity存账面数量，实际数量和差异数量从扩展字段解析
            item.setBookQuantity(entity.getQuantity());
            item.setUnitPrice(entity.getUnitPrice());
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
            item.setBookQuantity(itemResponse.getQuantity());
            item.setUnitPrice(itemResponse.getUnitPrice());
            item.setRemark(itemResponse.getRemark());
            item.setLineNo(itemResponse.getLineNo());
            return item;
        }
    }

    public static StockCheckDTO fromEntity(DocEntity entity, List<DocItemEntity> itemEntities) {
        StockCheckDTO dto = new StockCheckDTO();
        dto.setId(entity.getId());
        dto.setDocNo(entity.getDocNo());
        dto.setShopCode(entity.getShopCode());
        dto.setShopName(entity.getShopName());
        dto.setWarehouseId(entity.getWarehouseId());
        dto.setWarehouseCode(entity.getWarehouseCode());
        dto.setWarehouseName(entity.getWarehouseName());
        dto.setDocDate(entity.getDocDate());
        dto.setRemark(entity.getRemark());
        dto.setStatus(entity.getStatus());
        dto.setCreatorName(entity.getCreatorName());
        dto.setCreateTime(entity.getCreateTime());
        if (itemEntities != null) {
            dto.setItems(itemEntities.stream().map(Item::fromEntity).collect(Collectors.toList()));
        }
        return dto;
    }

    public static StockCheckDTO fromResponse(DocResponse response) {
        StockCheckDTO dto = new StockCheckDTO();
        dto.setId(response.getId());
        dto.setDocNo(response.getDocNo());
        dto.setShopCode(response.getShopCode());
        dto.setShopName(response.getShopName());
        dto.setWarehouseId(response.getWarehouseId());
        dto.setWarehouseCode(response.getWarehouseCode());
        dto.setWarehouseName(response.getWarehouseName());
        dto.setDocDate(response.getDocDate());
        dto.setRemark(response.getRemark());
        dto.setStatus(response.getStatus());
        dto.setCreatorName(response.getCreatorName());
        dto.setCreateTime(response.getCreateTime());
        if (response.getItems() != null) {
            dto.setItems(response.getItems().stream().map(Item::fromResponse).collect(Collectors.toList()));
        }
        return dto;
    }
}