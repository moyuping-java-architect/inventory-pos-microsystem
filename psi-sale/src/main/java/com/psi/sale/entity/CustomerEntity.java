package com.psi.sale.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer")
public class CustomerEntity extends BaseEntity {

    /**
     * 客户编码
     */
    private String customerCode;

    /**
     * 客户全称
     */
    private String customerName;

    /**
     * 客户简称
     */
    private String shortName;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系人电话
     */
    private String contactPhone;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 邮政编码
     */
    private String zipCode;

    /**
     * 税务登记号
     */
    private String taxNo;

    /**
     * 开户银行名称
     */
    private String bankName;

    /**
     * 银行账号
     */
    private String bankAccount;

    /**
     * 客户类型：1-普通客户 2-会员 3-批发商
     */
    private String customerType;

    /**
     * 客户等级：A/B/C/D
     */
    private String customerLevel;

    /**
     * 信用额度
     */
    private java.math.BigDecimal creditLimit;

    /**
     * 备注
     */
    private String remark;
}