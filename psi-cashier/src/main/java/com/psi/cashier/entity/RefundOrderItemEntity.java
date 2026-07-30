package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 退货单明细表实体类
 * 存储退货商品的详细信息
 */
@Data
@TableName("refund_order_item")
public class RefundOrderItemEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 退货明细ID
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
     * 退货单号
     */
    private String refundNo;

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
     * 退货数量
     */
    private BigDecimal refundQuantity;

    /**
     * 退货单价
     */
    private BigDecimal refundPrice;

    /**
     * 退款小计
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
     * 不含税净额
     */
    private BigDecimal netAmount;

    /**
     * 税额
     */
    private BigDecimal taxAmount;

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