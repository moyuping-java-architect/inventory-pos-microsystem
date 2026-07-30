package com.trademaster.controller;

import com.trademaster.common.Result;
import com.trademaster.dto.LoginDTO;
import com.trademaster.entity.SysUser;
import com.trademaster.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        String token = authService.login(dto.getUsername(), dto.getPassword());
        SysUser user = authService.getCurrentUser(dto.getUsername());
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("role", user.getRole());
        
        return Result.success(data);
    }

    @GetMapping("/profile")
    public Result<SysUser> profile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (String) auth.getPrincipal();
        return Result.success(authService.getCurrentUser(username));
    }
}
