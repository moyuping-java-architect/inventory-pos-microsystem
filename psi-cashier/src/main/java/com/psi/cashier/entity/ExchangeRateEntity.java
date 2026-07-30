package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 汇率实体
 */
@Data
@TableName("exchange_rate")
public class ExchangeRateEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 源币种
     */
    private String fromCurrency;

    /**
     * 目标币种
     */
    private String toCurrency;

    /**
     * 汇率
     */
    private BigDecimal rate;

    /**
     * 生效日期
     */
    private String effectiveDate;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 修改时间
     */
    private String updateTime;
}
