package com.psi.order.dto.biz.stock;

import com.psi.order.dto.DocResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Schema(description = "库存初始化单DTO")
public class InventoryInitDTO {

    @Schema(description = "草稿ID")
    private Long id;

    @Schema(description = "初始化单编号")
    private String docNo;

    @Schema(description = "商铺编码")
    private String shopCode;

    @Schema(description = "商铺名称")
    private String shopName;

    @Schema(description = "仓库编码")
    private String warehouseCode;

    @Schema(description = "仓库名称")
    private String warehouseName;

    @Schema(description = "单据日期")
    private LocalDateTime docDate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "单据状态")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "制单人ID")
    private String creatorId;

    @Schema(description = "制单人名称")
    private String creatorName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "商品项数")
    private Integer itemCount;

    @Schema(description = "总金额")
    private BigDecimal totalAmount;

    @Schema(description = "商品明细")
    private List<InventoryInitItemDTO> items;

    public static InventoryInitDTO fromResponse(DocResponse response) {
        InventoryInitDTO dto = new InventoryInitDTO();
        dto.setId(response.getId());
        dto.setDocNo(response.getDocNo());
        dto.setShopCode(response.getShopCode());
        dto.setShopName(response.getShopName());
        dto.setWarehouseCode(response.getWarehouseCode());
        dto.setWarehouseName(response.getWarehouseName());
        dto.setDocDate(response.getDocDate());
        dto.setRemark(response.getRemark());
        dto.setStatus(response.getStatus());
        dto.setStatusDesc(response.getStatusDesc());
        dto.setCreatorId(response.getCreatorId());
        dto.setCreatorName(response.getCreatorName());
        dto.setCreateTime(response.getCreateTime());
        dto.setUpdateTime(response.getUpdateTime());
        dto.setItemCount(response.getItemCount());
        dto.setTotalAmount(response.getTotalAmount());
        if (response.getItems() != null) {
            dto.setItems(response.getItems().stream()
                    .map(InventoryInitItemDTO::fromResponse)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    @Data
    @Schema(description = "库存初始化商品明细")
    public static class InventoryInitItemDTO {

        @Schema(description = "商品编码")
        private String goodsCode;

        @Schema(description = "商品名称")
        private String goodsName;

        @Schema(description = "规格")
        private String goodsSpec;

        @Schema(description = "单位")
        private String goodsUnit;

        @Schema(description = "初始化数量")
        private BigDecimal quantity;

        @Schema(description = "单价")
        private BigDecimal unitPrice;

        @Schema(description = "金额")
        private BigDecimal amount;

        @Schema(description = "备注")
        private String remark;

        public static InventoryInitItemDTO fromResponse(DocResponse.DocItemResponse item) {
            InventoryInitItemDTO dto = new InventoryInitItemDTO();
            dto.setGoodsCode(item.getGoodsCode());
            dto.setGoodsName(item.getGoodsName());
            dto.setGoodsSpec(item.getGoodsSpec());
            dto.setGoodsUnit(item.getGoodsUnit());
            dto.setQuantity(item.getQuantity());
            dto.setUnitPrice(item.getUnitPrice());
            dto.setAmount(item.getAmount());
            dto.setRemark(item.getRemark());
            return dto;
        }
    }
}