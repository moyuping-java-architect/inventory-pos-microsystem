package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 班次支付方式明细实体类
 */
@Data
@TableName("cashier_shift_pay")
public class CashierShiftPayEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 数据UUID（跨服务比对用）
     */
    private String dataUuid;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 门店编码
     */
    private String shopCode;

    /**
     * 收银机编号
     */
    private String posId;

    /**
     * 班次单号
     */
    private String shiftNo;

    /**
     * 支付方式ID
     */
    private Integer payId;

    /**
     * 支付方式名称
     */
    private String payName;

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 制单人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private String createTime;
}