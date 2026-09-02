package com.psi.order.dto.biz.goods;

import com.psi.order.dto.DocResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Schema(description = "调价单DTO")
public class AdjustPriceDTO {

    @Schema(description = "草稿ID")
    private Long id;

    @Schema(description = "调价单编号")
    private String docNo;

    @Schema(description = "商铺编码")
    private String shopCode;

    @Schema(description = "商铺名称")
    private String shopName;

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

    @Schema(description = "商品明细")
    private List<AdjustPriceItemDTO> items;

    public static AdjustPriceDTO fromResponse(DocResponse response) {
        AdjustPriceDTO dto = new AdjustPriceDTO();
        dto.setId(response.getId());
        dto.setDocNo(response.getDocNo());
        dto.setShopCode(response.getShopCode());
        dto.setShopName(response.getShopName());
        dto.setDocDate(response.getDocDate());
        dto.setRemark(response.getRemark());
        dto.setStatus(response.getStatus());
        dto.setStatusDesc(response.getStatusDesc());
        dto.setCreatorId(response.getCreatorId());
        dto.setCreatorName(response.getCreatorName());
        dto.setCreateTime(response.getCreateTime());
        dto.setUpdateTime(response.getUpdateTime());
        dto.setItemCount(response.getItemCount());
        if (response.getItems() != null) {
            dto.setItems(response.getItems().stream()
                    .map(AdjustPriceItemDTO::fromResponse)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    @Data
    @Schema(description = "调价商品明细")
    public static class AdjustPriceItemDTO {

        @Schema(description = "商品编码")
        private String goodsCode;

        @Schema(description = "商品名称")
        private String goodsName;

        @Schema(description = "规格")
        private String goodsSpec;

        @Schema(description = "单位")
        private String goodsUnit;

        @Schema(description = "新价格")
        private BigDecimal unitPrice;

        @Schema(description = "备注")
        private String remark;

        public static AdjustPriceItemDTO fromResponse(DocResponse.DocItemResponse item) {
            AdjustPriceItemDTO dto = new AdjustPriceItemDTO();
            dto.setGoodsCode(item.getGoodsCode());
            dto.setGoodsName(item.getGoodsName());
            dto.setGoodsSpec(item.getGoodsSpec());
            dto.setGoodsUnit(item.getGoodsUnit());
            dto.setUnitPrice(item.getUnitPrice());
            dto.setRemark(item.getRemark());
            return dto;
        }
    }
}