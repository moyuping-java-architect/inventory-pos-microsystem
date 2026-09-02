package com.psi.common.context;

import lombok.Data;

@Data
public class UserInfo {
    private String tenantId;
    private String shopId;
    private String warehouseId;
    private String updateUserId;
    private String updateUserName;
    private String roleId;
    private String roleCode;
    private String roleName;
    private String permissions;
}