package com.psi.cashier.constant;

/**
 * 业务类型枚举
 * 定义各种单据类型的编码和描述
 * 与 sys_seq 表中的 seq_type 字段对应
 * 
 * @author PSI
 * @version 1.0.0
 */
public enum BizTypeConstant {

    /**
     * 销售订单（对应数据库：XS）
     */
    SALE("XS", "销售", "销售订单"),

    /**
     * 退货订单（对应数据库：TH）
     */
    REFUND("TH", "退货", "退货订单"),

    /**
     * 挂单（对应数据库：JD）
     */
    PENDING("JD", "挂单", "挂单订单"),

    /**
     * 日结（对应数据库：RJ）
     */
    DAILY_SETTLE("RJ", "日结", "日结单"),

    /**
     * 收款
     */
    RECEIPT("SK", "收款", "收款单"),

    /**
     * 退款
     */
    PAY_REFUND("TK", "退款", "退款单"),

    /**
     * 库存盘点
     */
    INVENTORY("PD", "盘点", "盘点单"),

    /**
     * 报损单
     */
    LOSS("BS", "报损", "报损单"),

    /**
     * 报溢单
     */
    OVERFLOW("BY", "报溢", "报溢单");

    /**
     * 业务类型编码（2位），与数据库 seq_type 字段对应
     */
    private final String code;

    /**
     * 业务类型简称
     */
    private final String shortName;

    /**
     * 业务类型全称描述
     */
    private final String description;

    BizTypeConstant(String code, String shortName, String description) {
        this.code = code;
        this.shortName = shortName;
        this.description = description;
    }

    /**
     * 获取业务类型编码
     *
     * @return 编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取业务类型简称
     *
     * @return 简称
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * 获取业务类型描述
     *
     * @return 描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据编码获取业务类型枚举
     *
     * @param code 编码
     * @return 业务类型枚举，如果未找到返回null
     */
    public static BizTypeConstant fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (BizTypeConstant type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断是否为销售相关类型
     *
     * @return 是否销售类型
     */
    public boolean isSaleRelated() {
        return this == SALE || this == REFUND;
    }

    /**
     * 判断是否为库存相关类型
     *
     * @return 是否库存类型
     */
    public boolean isInventoryRelated() {
        return this == INVENTORY || this == LOSS || this == OVERFLOW;
    }
}