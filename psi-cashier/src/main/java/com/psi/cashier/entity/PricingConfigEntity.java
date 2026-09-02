package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 定价配置实体
 *
 * 三层加价模型：
 * 1. 采购端：ZMW成本 ÷ purchaseRate(低于市场) = USD成本
 * 2. 加价端：USD成本 × markupFactor = USD售价
 * 3. 销售端：USD售价 × salesRate(高于市场) = ZMW零售价
 *
 * 客户只看到ZMW零售价，三层加价全隐藏在后台。
 *
 * @author PSI
 * @version 1.0.0
 */
@Data
@TableName("pricing_config")
public class PricingConfigEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 基准币种（默认USD，所有成本以USD为锚）
     */
    private String baseCurrency;

    /**
     * 本地币种（默认ZMW，客户看到的标价币种）
     */
    private String localCurrency;

    /**
     * 加价系数（默认1.26，即26%成本加价）
     * USD成本 × markupFactor = USD售价
     */
    private BigDecimal markupFactor;

    /**
     * 采购汇率（ZMW→USD换算用，低于市场汇率）
     * 例：市场汇率27.0，采购汇率设26.0
     * USD成本 = ZMW采购价 ÷ purchaseRate
     * 除以小数 = USD成本算高 = 利润空间大
     */
    private BigDecimal purchaseRate;

    /**
     * 销售汇率（USD→ZMW换算用，高于市场汇率）
     * 例：市场汇率27.0，销售汇率设28.0
     * ZMW零售价 = USD售价 × salesRate
     * 乘以大数 = ZMW售价算高 = 多收钱
     */
    private BigDecimal salesRate;

    /**
     * 市场实际汇率（用于成本核算和利润分析）
     * 例：27.0（银行实际换汇率）
     * 利润分析时用这个算真实成本
     */
    private BigDecimal marketRate;

    /**
     * 是否自动更新零售价（0:否 1:是）
     * 汇率变动时自动重算所有商品ZMW零售价
     */
    private Integer autoUpdateRetail;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 修改时间
     */
    private String updateTime;
}
