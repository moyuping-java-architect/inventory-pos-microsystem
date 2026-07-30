package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("order_pending_item")
public class OrderPendingItemEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据UUID（跨服务比对用）
     */
    private String dataUuid;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 店铺编码
     */
    private String shopCode;

    /**
     * 收银机编号
     */
    private String posId;

    /**
     * 挂单号
     */
    private String pendingNo;

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
     * 条码
     */
    private String barCode;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 销售单位名称
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
     * 小计
     */
    private BigDecimal subtotal;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private String updateTime;
}