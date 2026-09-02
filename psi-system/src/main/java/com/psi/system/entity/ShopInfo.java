package com.psi.system.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("shop_info")
public class ShopInfo extends BaseEntity {

    private String shopName;

    private String shopCode;

    private String address;

    private String phone;

    private String manager;
}