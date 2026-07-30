package com.psi.system.entity;

import com.psi.common.mybatis.entity.BaseNoTenantEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_tenant")
public class SysTenant extends BaseNoTenantEntity {

    private String tenantName;

    private String tenantCode;

    private String contactName;

    private String contactPhone;

    private String email;

    private String address;

    private LocalDateTime expireTime;
}