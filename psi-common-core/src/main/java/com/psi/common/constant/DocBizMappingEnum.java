package com.psi.common.constant;

import java.util.Arrays;
import java.util.List;

/**
 * 单据类型与业务类型映射枚举
 *
 * <p>统一管理单据类型（DocType）到业务类型（BizType）的映射关系，
 * 用于流程完成后的MQ路由决策。
 *
 * <p>后续可扩展为Nacos配置，通过配置中心动态管理映射关系。
 *
 * @author PSI
 * @version 1.0.0
 */
public enum DocBizMappingEnum {

    PURCHASE_ORDER("PURCHASE_ORDER", "采购订单", BizTypeEnum.PURCHASE),
    PURCHASE_IN("PURCHASE_IN", "采购入库单", BizTypeEnum.PURCHASE),
    PURCHASE_RETURN("PURCHASE_RETURN", "采购退货单", BizTypeEnum.PURCHASE),

    SALE_ORDER("SALE_ORDER", "销售订单", BizTypeEnum.SALE),
    SALE_OUT("SALE_OUT", "销售出库单", BizTypeEnum.SALE),
    SALE_RETURN("SALE_RETURN", "销售退货单", BizTypeEnum.SALE),

    STOCK_LOSS("STOCK_LOSS", "报损单", BizTypeEnum.LOSS),
    STOCK_OVERFLOW("STOCK_OVERFLOW", "报溢单", BizTypeEnum.OVERFLOW),
    STOCK_CHECK("STOCK_CHECK", "盘点单", BizTypeEnum.CHECK),
    STOCK_TRANSFER("STOCK_TRANSFER", "调拨单", BizTypeEnum.STOCK),
    INVENTORY_INIT("INVENTORY_INIT", "库存初始化单", BizTypeEnum.STOCK),
    ADJUST_PRICE("ADJUST_PRICE", "调价单", BizTypeEnum.STOCK);

    private final String docType;
    private final String docDescription;
    private final BizTypeEnum bizType;

    DocBizMappingEnum(String docType, String docDescription, BizTypeEnum bizType) {
        this.docType = docType;
        this.docDescription = docDescription;
        this.bizType = bizType;
    }

    public String getDocType() {
        return docType;
    }

    public String getDocDescription() {
        return docDescription;
    }

    public BizTypeEnum getBizType() {
        return bizType;
    }

    public String getBizTypeCode() {
        return bizType.getCode();
    }

    /**
     * 业务类型枚举
     * <p>与order-rule模块中的BizType对应，抽取到common-core便于跨模块引用
     */
    public enum BizTypeEnum {
        PURCHASE("PURCHASE", "采购业务"),
        SALE("SALE", "销售业务"),
        LOSS("LOSS", "报损业务"),
        OVERFLOW("OVERFLOW", "报溢业务"),
        CHECK("CHECK", "盘点业务"),
        STOCK("STOCK", "库存业务"),
        GOODS("GOODS", "商品业务"),
        COMMON("COMMON", "通用业务");

        private final String code;
        private final String description;

        BizTypeEnum(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static BizTypeEnum fromCode(String code) {
            for (BizTypeEnum type : values()) {
                if (type.code.equalsIgnoreCase(code)) {
                    return type;
                }
            }
            return COMMON;
        }
    }

    /**
     * 根据单据类型获取对应的业务类型
     *
     * @param docType 单据类型编码（如 "PURCHASE_ORDER"）
     * @return 业务类型编码，未匹配时返回 COMMON
     */
    public static String getBizTypeCode(String docType) {
        if (docType == null || docType.trim().isEmpty()) {
            return BizTypeEnum.COMMON.getCode();
        }
        for (DocBizMappingEnum mapping : values()) {
            if (mapping.docType.equalsIgnoreCase(docType.trim())) {
                return mapping.bizType.getCode();
            }
        }
        return BizTypeEnum.COMMON.getCode();
    }

    /**
     * 根据单据类型获取对应的业务类型枚举
     *
     * @param docType 单据类型编码
     * @return BizTypeEnum，未匹配时返回 COMMON
     */
    public static BizTypeEnum getBizType(String docType) {
        if (docType == null || docType.trim().isEmpty()) {
            return BizTypeEnum.COMMON;
        }
        for (DocBizMappingEnum mapping : values()) {
            if (mapping.docType.equalsIgnoreCase(docType.trim())) {
                return mapping.bizType;
            }
        }
        return BizTypeEnum.COMMON;
    }

    /**
     * 获取指定业务类型下的所有单据类型
     *
     * @param bizType 业务类型
     * @return 单据类型编码列表
     */
    public static List<String> getDocTypes(BizTypeEnum bizType) {
        if (bizType == null) {
            return List.of();
        }
        return Arrays.stream(values())
                .filter(m -> m.bizType == bizType)
                .map(DocBizMappingEnum::getDocType)
                .toList();
    }
}