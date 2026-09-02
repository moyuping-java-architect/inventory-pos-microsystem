package com.psi.goods.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.psi.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品调价单主表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_adjust_price_main")
public class AdjustPriceMainEntity extends BaseEntity {

    /**
     * 调价单号
     */
    private String adjustNo;

    /**
     * 单据名称
     */
    @TableField("doc_name")
    private String docName;

    /**
     * 商铺编码
     */
    @TableField("shop_code")
    private String shopCode;

    /**
     * 商铺名称
     */
    @TableField("shop_name")
    private String shopName;

    /**
     * 调价日期
     */
    @TableField("adjust_date")
    private String adjustDate;

    /**
     * 总金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 明细项数
     */
    @TableField("item_count")
    private Integer itemCount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态(0:草稿 1:已提交 2:已审核)
     */
    private Integer status;

    /**
     * 明细列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<AdjustPriceItemEntity> items;
}
