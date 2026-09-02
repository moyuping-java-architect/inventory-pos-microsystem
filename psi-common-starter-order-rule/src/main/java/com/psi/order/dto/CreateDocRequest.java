package com.psi.order.dto;

import com.psi.order.constant.DocTypeConstant.DocType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建单据请求DTO
 */
@Data
public class CreateDocRequest {

    /**
     * 单据类型
     */
    private String docType = DocType.PURCHASE_ORDER.getCode();

    /**
     * 单据名称（必填，默认：单据类型+当天日期）
     */
    private String docName;

    /**
     * 商铺编码
     */
    private String shopCode;

    /**
     * 商铺名称
     */
    private String shopName;

    /**
     * 创建人ID
     */
    private String creatorId;

    /**
     * 创建人姓名
     */
    private String creatorName;

    /**
     * 部门ID
     */
    private String deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 供应商/客户ID
     */
    private String partnerId;

    /**
     * 供应商/客户编码
     */
    private String partnerCode;

    /**
     * 供应商/客户名称
     */
    private String partnerName;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 仓库编码
     */
    private String warehouseCode;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 关联订单号（用于入库/出库等关联上游单据）
     */
    private String orderNo;

    /**
     * 销售类型：1-普通销售 2-批发 3-零售
     */
    private Integer saleType;

    /**
     * 付款方式：1-预付定金 2-货到付款 3-月结 4-现金 5-刷卡 6-赊销
     */
    private Integer paymentType;

    /**
     * 货币编码
     */
    private String currencyCode;

    /**
     * 汇率
     */
    private BigDecimal exchangeRate;

    /**
     * 税额
     */
    private BigDecimal taxAmount;

    /**
     * 折扣金额
     */
    private BigDecimal discountAmount;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 单据日期
     */
    private LocalDateTime docDate;

    /**
     * 交货/预计到货日期
     */
    private LocalDateTime deliveryDate;

    /**
     * 备注
     */
    private String remark;

    /**
     * 扩展字段（JSON格式）
     */
    private String extJson;

    /**
     * 单据明细列表
     */
    private List<DocItemRequest> items;

    /**
     * 单据明细请求DTO
     */
    @Data
    public static class DocItemRequest {

        /**
         * 商品ID
         */
        private Long goodsId;

        /**
         * 商品编码
         */
        private String goodsCode;

        /**
         * SKU编码
         */
        private String skuCode;

        /**
         * SKU名称
         */
        private String skuName;

        /**
         * 条码
         */
        private String barcode;

        /**
         * 商品名称
         */
        private String goodsName;

        /**
         * 商品规格
         */
        private String goodsSpec;

        /**
         * 计量单位编码
         */
        private String unitCode;

        /**
         * 商品单位名称
         */
        private String goodsUnit;

        /**
         * 销售单位到库存基础单位的换算率
         */
        private BigDecimal conversionRate;

        /**
         * 单价（不含税）
         */
        private BigDecimal unitPrice;

        /**
         * 数量
         */
        private BigDecimal quantity;

        /**
         * 税率(%)
         */
        private BigDecimal taxRate;

        /**
         * 折扣率(%)
         */
        private BigDecimal discountRate;

        /**
         * 折扣金额
         */
        private BigDecimal discountAmount;

        /**
         * 成本价
         */
        private BigDecimal costPrice;

        /**
         * 库存ID
         */
        private Long stockId;

        /**
         * 批次号
         */
        private String batchNo;

        /**
         * 有效期至
         */
        private String expiryDate;

        /**
         * 备注
         */
        private String remark;

        /**
         * 行号
         */
        private Integer lineNo;
    }
}