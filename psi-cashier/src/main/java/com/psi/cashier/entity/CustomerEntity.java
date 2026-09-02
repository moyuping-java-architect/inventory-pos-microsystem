package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 客户实体类
 * 存储从后台同步的客户数据，用于POS收银时选择客户、挂账、会员转化
 */
@Data
@TableName("customer")
public class CustomerEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("data_uuid")
    private String dataUuid;

    @TableField("data_version")
    private Long dataVersion;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("customer_code")
    private String customerCode;

    @TableField("customer_name")
    private String customerName;

    @TableField("short_name")
    private String shortName;

    @TableField("contact_name")
    private String contactName;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("email")
    private String email;

    @TableField("address")
    private String address;

    @TableField("province")
    private String province;

    @TableField("city")
    private String city;

    @TableField("district")
    private String district;

    @TableField("zip_code")
    private String zipCode;

    @TableField("tax_no")
    private String taxNo;

    @TableField("bank_name")
    private String bankName;

    @TableField("bank_account")
    private String bankAccount;

    @TableField("customer_type")
    private String customerType;

    @TableField("customer_level")
    private String customerLevel;

    @TableField("credit_limit")
    private Double creditLimit;

    @TableField("remark")
    private String remark;

    @TableField("del_flag")
    private Integer delFlag;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private String createTime;

    @TableField("update_time")
    private String updateTime;
}