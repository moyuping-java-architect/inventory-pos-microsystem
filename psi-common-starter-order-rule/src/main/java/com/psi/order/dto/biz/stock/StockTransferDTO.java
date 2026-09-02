package com.psi.order.dto.biz.stock;

import com.psi.order.entity.DocEntity;
import com.psi.order.entity.DocItemEntity;
import com.psi.order.dto.DocResponse;
import com.psi.common.util.JsonUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 调拨单DTO
 * 参照 stock_transfer_main / stock_transfer_item 正式表结构
 */
@Data
@Slf4j
@Schema(description = "调拨单DTO")
public class StockTransferDTO {

    @Schema(description = "草稿ID")
    private Long id;

    @Schema(description = "调拨单编号")
    private String docNo;

    @Schema(description = "商铺编码")
    private String shopCode;

    @Schema(description = "商铺名称")
    private String shopName;

    @Schema(description = "调出仓库ID")
    private Long fromWarehouseId;

    @Schema(description = "调出仓库编码")
    private String fromWarehouseCode;

    @Schema(description = "调出仓库名称")
    private String fromWarehouseName;

    @Schema(description = "调入仓库ID")
    private Long toWarehouseId;

    @Schema(description = "调入仓库编码")
    private String toWarehouseCode;

    @Schema(description = "调入仓库名称")
    private String toWarehouseName;

    @Schema(description = "调拨日期")
    private LocalDateTime docDate;

    @Schema(description = "调拨总金额")
    private BigDecimal totalAmount;

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
    @Schema(description = "调拨明细")
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

        @Schema(description = "调拨数量")
        private BigDecimal quantity;

        @Schema(description = "单价")
        private BigDecimal unitPrice;

        @Schema(description = "金额")
        private BigDecimal amount;

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
            item.setRemark(itemResponse.getRemark());
            item.setLineNo(itemResponse.getLineNo());
            return item;
        }
    }

    public static StockTransferDTO fromEntity(DocEntity entity, List<DocItemEntity> itemEntities) {
        StockTransferDTO dto = new StockTransferDTO();
        dto.setId(entity.getId());
        dto.setDocNo(entity.getDocNo());
        dto.setShopCode(entity.getShopCode());
        dto.setShopName(entity.getShopName());
        // 调拨单特殊：调出仓库用warehouse_id，调入仓库放在ext_json
        dto.setFromWarehouseId(entity.getWarehouseId());
        dto.setFromWarehouseCode(entity.getWarehouseCode());
        dto.setFromWarehouseName(entity.getWarehouseName());
        parseToWarehouse(dto, entity.getExtJson());
        dto.setDocDate(entity.getDocDate());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setStatus(entity.getStatus());
        dto.setRemark(entity.getRemark());
        dto.setCreatorName(entity.getCreatorName());
        dto.setCreateTime(entity.getCreateTime());
        if (itemEntities != null) {
            dto.setItems(itemEntities.stream().map(Item::fromEntity).collect(Collectors.toList()));
        }
        return dto;
    }

    public static StockTransferDTO fromResponse(DocResponse response) {
        StockTransferDTO dto = new StockTransferDTO();
        dto.setId(response.getId());
        dto.setDocNo(response.getDocNo());
        dto.setShopCode(response.getShopCode());
        dto.setShopName(response.getShopName());
        dto.setFromWarehouseId(response.getWarehouseId());
        dto.setFromWarehouseCode(response.getWarehouseCode());
        dto.setFromWarehouseName(response.getWarehouseName());
        parseToWarehouse(dto, response.getExtJson());
        dto.setDocDate(response.getDocDate());
        dto.setTotalAmount(response.getTotalAmount());
        dto.setStatus(response.getStatus());
        dto.setRemark(response.getRemark());
        dto.setCreatorName(response.getCreatorName());
        dto.setCreateTime(response.getCreateTime());
        if (response.getItems() != null) {
            dto.setItems(response.getItems().stream().map(Item::fromResponse).collect(Collectors.toList()));
        }
        return dto;
    }

    /**
     * 从 extJson 中解析调入仓库信息
     */
    @SuppressWarnings("unchecked")
    private static void parseToWarehouse(StockTransferDTO dto, String extJson) {
        if (extJson == null || extJson.isBlank()) {
            return;
        }
        try {
            Map<String, Object> ext = JsonUtils.fromJson(extJson, Map.class);
            if (ext == null) {
                return;
            }
            Object toWarehouseId = ext.get("toWarehouseId");
            if (toWarehouseId != null) {
                dto.setToWarehouseId(Long.valueOf(toWarehouseId.toString()));
            }
            Object toWarehouseName = ext.get("toWarehouseName");
            if (toWarehouseName != null) {
                dto.setToWarehouseName(toWarehouseName.toString());
            }
        } catch (Exception e) {
            log.warn("解析调拨单 extJson 失败: {}", extJson, e);
        }
    }
}