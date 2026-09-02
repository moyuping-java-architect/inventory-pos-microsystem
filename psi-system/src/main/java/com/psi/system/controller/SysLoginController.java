package com.psi.system.controller;

import com.psi.system.dto.SysLoginDTO;
import com.psi.system.dto.SysLoginResponseDTO;
import com.psi.system.service.SysLoginService;
import com.psi.common.constant.TenantMdcConstant;
import com.psi.common.result.CommonResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/admin")
public class SysLoginController {

    private final SysLoginService sysLoginService;

    public SysLoginController(SysLoginService sysLoginService) {
        this.sysLoginService = sysLoginService;
    }

    @PostMapping("/login")
    public ResponseEntity<CommonResult<SysLoginResponseDTO>> login(@Valid @RequestBody SysLoginDTO loginDTO) {
        CommonResult<SysLoginResponseDTO> result = sysLoginService.login(loginDTO);
        
        if (result.getCode() == 200) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + result.getData().getToken());
            headers.add(TenantMdcConstant.HEADER_UPDATE_USER_ID, result.getData().getUserInfo().getId().toString());
            headers.add(TenantMdcConstant.HEADER_TENANT_ID, result.getData().getUserInfo().getTenantId() != null ? result.getData().getUserInfo().getTenantId().toString() : null);
            headers.add(TenantMdcConstant.HEADER_SHOP_ID, result.getData().getUserInfo().getShopId() != null ? result.getData().getUserInfo().getShopId().toString() : null);
            headers.add(TenantMdcConstant.HEADER_WAREHOUSE_ID, result.getData().getUserInfo().getWarehouseId() != null ? result.getData().getUserInfo().getWarehouseId().toString() : null);
            headers.add(TenantMdcConstant.HEADER_ROLE_ID, result.getData().getUserInfo().getRoleId() != null ? result.getData().getUserInfo().getRoleId().toString() : null);
            headers.add(TenantMdcConstant.HEADER_ROLE_NAME, result.getData().getUserInfo().getRoleName());
            headers.add(TenantMdcConstant.HEADER_PERMISSIONS, result.getData().getUserInfo().getPermissions());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(result);
        }
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<CommonResult<Void>> logout() {
        CommonResult<Void> result = sysLoginService.logout();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "");
        return ResponseEntity.ok()
                .headers(headers)
                .body(result);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<CommonResult<SysLoginResponseDTO>> refreshToken(
            @RequestHeader("Refresh-Token") String refreshToken) {
        CommonResult<SysLoginResponseDTO> result = sysLoginService.refreshToken(refreshToken);
        
        if (result.getCode() == 200) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + result.getData().getToken());
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(result);
        }
        
        return ResponseEntity.ok(result);
    }
}