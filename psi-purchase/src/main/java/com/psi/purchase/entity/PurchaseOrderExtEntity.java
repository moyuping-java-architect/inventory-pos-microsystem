package com.psi.purchase.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_order_ext")
public class PurchaseOrderExtEntity extends BaseEntity {

    /**
     * 订单主表ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 扩展字段键
     */
    private String extKey;

    /**
     * 扩展字段值
     */
    private String extValue;

    /**
     * 扩展字段描述
     */
    private String extDesc;
}