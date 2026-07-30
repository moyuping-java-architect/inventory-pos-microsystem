package com.trademaster.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.trademaster.entity.SysUser;
import com.trademaster.mapper.SysUserMapper;
import com.trademaster.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(SysUserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String login(String username, String password) {
        SysUser user = userMapper.selectOne(new QueryWrapper<SysUser>().eq("username", username));
        
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            throw new RuntimeException("用户已禁用");
        }

        return jwtUtil.generateToken(user.getUsername(), user.getRole());
    }

    public SysUser getCurrentUser(String username) {
        return userMapper.selectOne(new QueryWrapper<SysUser>().eq("username", username));
    }
}
