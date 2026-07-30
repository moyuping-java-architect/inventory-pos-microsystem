package com.psi.purchase.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supplier")
public class SupplierEntity extends BaseEntity {

    /**
     * 供应商编码
     */
    private String supplierCode;

    /**
     * 供应商全称
     */
    private String supplierName;

    /**
     * 供应商简称
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
     * 供应商类型：1-普通供应商 2-核心供应商 3-战略供应商
     */
    private String supplierType;

    /**
     * 所属行业
     */
    private String industry;

    /**
     * 信用等级：A/B/C/D
     */
    private String creditLevel;

    /**
     * 备注
     */
    private String remark;
}