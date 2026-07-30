package com.psi.cashier.controller;

import com.psi.cashier.dto.LoginDTO;
import com.psi.cashier.dto.LoginResultDTO;
import com.psi.cashier.service.CashierAuthService;
import com.psi.common.constant.TenantMdcConstant;
import com.psi.common.result.CommonResult;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 收银认证控制器
 * 提供登录、登出等接口
 */
@RestController
@RequestMapping("/psi/cashier/auth")
@RequiredArgsConstructor
public class CashierAuthController {

    private final CashierAuthService cashierAuthService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public CommonResult<LoginResultDTO> login(@RequestBody LoginDTO dto, HttpServletResponse response) {
        LoginResultDTO result = cashierAuthService.login(dto);
        if (result.getSuccess()) {
            // 设置响应头，让前端在后续请求中携带这些信息
            LoginResultDTO.UserInfo userInfo = result.getUser();
            if (userInfo != null) {
                response.setHeader(TenantMdcConstant.HEADER_TENANT_ID, userInfo.getTenantId());
                response.setHeader(TenantMdcConstant.HEADER_SHOP_ID, userInfo.getShopCode());
                response.setHeader(TenantMdcConstant.HEADER_UPDATE_USER_ID, String.valueOf(userInfo.getId()));
                response.setHeader(TenantMdcConstant.HEADER_UPDATE_USER_NAME, userInfo.getRealName());
            }
            return CommonResult.success(result);
        } else {
            return CommonResult.fail(result.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public CommonResult<Void> logout(@RequestHeader("Authorization") String token) {
        cashierAuthService.logout(token);
        return CommonResult.success();
    }

    /**
     * 验证Token
     */
    @GetMapping("/validate")
    public CommonResult<LoginResultDTO.UserInfo> validate(@RequestHeader("Authorization") String token) {
        boolean valid = cashierAuthService.validateToken(token);
        if (valid) {
            return CommonResult.success(cashierAuthService.getCurrentUser(token));
        } else {
            return CommonResult.fail("Token无效");
        }
    }
}