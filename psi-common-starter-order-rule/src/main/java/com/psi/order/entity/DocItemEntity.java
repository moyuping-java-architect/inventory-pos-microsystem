package com.psi.order.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 通用单据明细实体类
 * 适用于所有单据类型的明细信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_item_draft")
public class DocItemEntity extends BaseEntity {

    /**
     * 商铺编码
     */
    private String shopCode;

    /**
     * 商铺名称
     */
    private String shopName;

    /**
     * 单据ID
     */
    private Long docId;

    /**
     * 单据编号
     */
    private String docNo;

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
     * 金额（不含税，单价*数量）
     */
    private BigDecimal amount;

    /**
     * 税率(%)
     */
    private BigDecimal taxRate;

    /**
     * 税额
     */
    private BigDecimal taxAmount;

    /**
     * 折扣率(%)
     */
    private BigDecimal discountRate;

    /**
     * 折扣金额
     */
    private BigDecimal discountAmount;

    /**
     * 净金额（含税）
     */
    private BigDecimal netAmount;

    /**
     * 成本价
     */
    private BigDecimal costPrice;

    /**
     * 成本金额
     */
    private BigDecimal costAmount;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

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

    /**
     * 计算金额（单价*数量）
     */
    public void calculateAmount() {
        if (unitPrice != null && quantity != null) {
            this.amount = unitPrice.multiply(quantity);
        }
    }

    /**
     * 计算成本金额
     */
    public void calculateCostAmount() {
        if (costPrice != null && quantity != null) {
            this.costAmount = costPrice.multiply(quantity);
        }
    }

    /**
     * 计算税额
     */
    public void calculateTaxAmount() {
        if (amount != null && taxRate != null) {
            this.taxAmount = amount.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        }
    }

    /**
     * 计算净金额（含税）
     */
    public void calculateNetAmount() {
        if (amount != null && taxAmount != null) {
            this.netAmount = amount.add(taxAmount);
        } else if (amount != null) {
            this.netAmount = amount;
        }
    }

    /**
     * 计算折扣金额
     */
    public void calculateDiscountAmount() {
        if (amount != null && discountRate != null) {
            this.discountAmount = amount.multiply(discountRate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        }
    }

    /**
     * 计算实付金额
     */
    public void calculatePayAmount() {
        if (netAmount != null) {
            this.payAmount = discountAmount != null ? netAmount.subtract(discountAmount) : netAmount;
        } else if (amount != null) {
            this.payAmount = discountAmount != null ? amount.subtract(discountAmount) : amount;
        }
    }

    /**
     * 执行所有金额计算
     */
    public void calculateAll() {
        calculateAmount();
        calculateTaxAmount();
        calculateNetAmount();
        calculateDiscountAmount();
        calculatePayAmount();
        calculateCostAmount();
    }
}