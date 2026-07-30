package com.psi.cashier.dto;

import com.psi.cashier.validation.ValidAmount;
import com.psi.cashier.validation.ValidQuantity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 收银明细保存 DTO
 * 支持前端手动输入数量、单价、金额等字段
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
public class CashierItemSaveDTO {

    /**
     * 商品编码
     */
    @NotBlank(message = "商品编码不能为空")
    private String goodsCode;

    /**
     * 商品条码
     */
    private String barCode;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    private String goodsName;

    /**
     * 商品规格
     */
    private String goodsSpec;

    /**
     * 单位
     */
    @NotBlank(message = "单位不能为空")
    private String unit;

    /**
     * 数量（支持手写输入，最小0.01，最大9999999999.9999，最多4位小数）
     */
    @ValidQuantity(message = "数量校验失败")
    private BigDecimal quantity;

    /**
     * 单价（支持手写输入，最小0.01，最大9999999999.9999，最多4位小数）
     */
    @ValidQuantity(message = "单价格式不正确")
    private BigDecimal unitPrice;

    /**
     * 金额（支持手写输入，最小0.01，最大9999999999.99，最多2位小数）
     */
    @ValidAmount(message = "金额校验失败")
    private BigDecimal amount;

    /**
     * VAT税率（如0.1600表示16%）
     */
    private BigDecimal taxRate;

    /**
     * 标价是否含税(0:否 1:是)
     */
    private Integer isTaxInclusive;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 行级币种（与主单一致）
     */
    private String currency;

    /**
     * USD 单价（切换币种时参考）
     */
    private BigDecimal unitPriceUsd;

    /**
     * 仓库编码
     */
    private String warehouseCode;

    /**
     * SKU ID（数字类型）
     */
    private String skuId;
}