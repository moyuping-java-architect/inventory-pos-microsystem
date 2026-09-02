package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销售订单明细表实体类
 */
@Data
@TableName("order_item")
public class OrderItemEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 明细ID
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
     * 订单号
     */
    private String orderNo;

    /**
     * 业务类型
     */
    private Integer bizType;

    /**
     * SKU ID
     */
    private Integer skuId;

    /**
     * SKU编码
     */
    private String skuCode;

    /**
     * 商品条码
     */
    private String barCode;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 销售单位
     */
    private String saleUnitName;

    /**
     * 销售数量
     */
    private BigDecimal saleQuantity;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 会员价
     */
    private BigDecimal memberPrice;

    /**
     * 小计金额（含税）
     */
    private BigDecimal subtotal;

    /**
     * VAT税率
     */
    private BigDecimal taxRate;

    /**
     * 标价是否含税(0:否 1:是)
     */
    private Integer isTaxInclusive;

    /**
     * 税额
     */
    private BigDecimal taxAmount;

    /**
     * 不含税净额
     */
    private BigDecimal netAmount;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 币种
     */
    private String currency;

    /**
     * 制单人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 修改时间
     */
    private String updateTime;
}