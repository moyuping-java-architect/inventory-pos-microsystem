package com.psi.order.constant;

/**
 * 单据类型常量类
 * 定义所有业务单据类型
 */
public class DocTypeConstant {

    /**
     * 业务类型枚举（用于MQ路由）
     */
    public enum BizType {
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

        BizType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static BizType fromCode(String code) {
            for (BizType type : values()) {
                if (type.code.equalsIgnoreCase(code)) {
                    return type;
                }
            }
            return COMMON;
        }
    }

    /**
     * 单据类型枚举
     */
    public enum DocType {
        PURCHASE_ORDER("PURCHASE_ORDER", "采购订单", "PO"),
        PURCHASE_IN("PURCHASE_IN", "采购入库单", "PIN"),
        PURCHASE_RETURN("PURCHASE_RETURN", "采购退货单", "PRT"),
        
        SALE_ORDER("SALE_ORDER", "销售订单", "SO"),
        SALE_OUT("SALE_OUT", "销售出库单", "SOUT"),
        SALE_RETURN("SALE_RETURN", "销售退货单", "SRT"),
        
        STOCK_LOSS("STOCK_LOSS", "报损单", "LOSS"),
        STOCK_OVERFLOW("STOCK_OVERFLOW", "报溢单", "OVER"),
        STOCK_CHECK("STOCK_CHECK", "盘点单", "CHECK"),
        STOCK_TRANSFER("STOCK_TRANSFER", "调拨单", "TF"),
        
        INVENTORY_INIT("INVENTORY_INIT", "库存初始化单", "INIT"),
        ADJUST_PRICE("ADJUST_PRICE", "调价单", "ADJ");

        private final String code;
        private final String description;
        private final String prefix;

        DocType(String code, String description, String prefix) {
            this.code = code;
            this.description = description;
            this.prefix = prefix;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public String getPrefix() {
            return prefix;
        }

        public static DocType fromCode(String code) {
            for (DocType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            return PURCHASE_ORDER;
        }

        public static DocType fromPrefix(String prefix) {
            for (DocType type : values()) {
                if (type.prefix.equalsIgnoreCase(prefix)) {
                    return type;
                }
            }
            return PURCHASE_ORDER;
        }

        public boolean isPurchase() {
            return this.code.startsWith("PURCHASE");
        }

        public boolean isSale() {
            return this.code.startsWith("SALE");
        }

        public boolean isStockAdjust() {
            return this == STOCK_LOSS || this == STOCK_OVERFLOW;
        }

        public boolean isOutbound() {
            return this == SALE_OUT || this == STOCK_TRANSFER;
        }

        public boolean isInbound() {
            return this == PURCHASE_IN || this == SALE_RETURN;
        }
    }

    /**
     * 单据状态枚举
     */
    public enum DocStatus {
        DRAFT("DRAFT", "草稿", 0),
        SUBMITTED("SUBMITTED", "已提交", 1),
        APPROVING("APPROVING", "审批中", 2),
        APPROVED("APPROVED", "已审批", 3),
        EXECUTING("EXECUTING", "执行中", 4),
        COMPLETED("COMPLETED", "已完成", 5),
        CANCELLED("CANCELLED", "已取消", -1),
        REJECTED("REJECTED", "已驳回", -2);

        private final String code;
        private final String description;
        private final int value;

        DocStatus(String code, String description, int value) {
            this.code = code;
            this.description = description;
            this.value = value;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public int getValue() {
            return value;
        }

        public static DocStatus fromCode(String code) {
            for (DocStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            return DRAFT;
        }

        public static DocStatus fromValue(int value) {
            for (DocStatus status : values()) {
                if (status.value == value) {
                    return status;
                }
            }
            return DRAFT;
        }

        public boolean canSubmit() {
            return this == DRAFT || this == REJECTED;
        }

        public boolean canApprove() {
            return this == SUBMITTED || this == APPROVING;
        }

        public boolean canExecute() {
            return this == APPROVED;
        }

        public boolean canCancel() {
            return this != CANCELLED && this != COMPLETED;
        }
    }

    public static final int MAX_ITEM_COUNT = 9999;

    public static final int DEFAULT_TIMEOUT_MINUTES = 180;
}