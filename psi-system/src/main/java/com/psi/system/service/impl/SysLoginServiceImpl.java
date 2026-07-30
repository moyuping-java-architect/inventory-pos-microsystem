package com.psi.system.service.impl;

import com.psi.system.dto.SysLoginDTO;
import com.psi.system.dto.SysLoginLogMessageDTO;
import com.psi.system.dto.SysLoginResponseDTO;
import com.psi.system.dto.SysUserDTO;
import com.psi.system.entity.SysDept;
import com.psi.system.entity.SysRole;
import com.psi.system.entity.SysRoleMenu;
import com.psi.system.entity.SysUser;
import com.psi.system.entity.SysUserRole;
import com.psi.system.mapper.SysDeptMapper;
import com.psi.system.mapper.SysMenuMapper;
import com.psi.system.mapper.SysRoleMapper;
import com.psi.system.mapper.SysRoleMenuMapper;
import com.psi.system.mapper.SysUserMapper;
import com.psi.system.mapper.SysUserRoleMapper;
import com.psi.system.service.SysLoginService;
import com.psi.system.util.JwtUtil;
import com.psi.common.async.facade.MqMessageFacade;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.context.UserContext;
import com.psi.common.context.UserInfo;
import com.psi.common.message.MessageFactory;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.result.CommonResult;
import com.psi.common.util.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class SysLoginServiceImpl implements SysLoginService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysMenuMapper sysMenuMapper;
    private final SysDeptMapper sysDeptMapper;
    private final JwtUtil jwtUtil;
    private final MqMessageFacade mqMessageFacade;

    public SysLoginServiceImpl(SysUserMapper sysUserMapper,
                               SysUserRoleMapper sysUserRoleMapper,
                               SysRoleMapper sysRoleMapper,
                               SysRoleMenuMapper sysRoleMenuMapper,
                               SysMenuMapper sysMenuMapper,
                               SysDeptMapper sysDeptMapper,
                               JwtUtil jwtUtil,
                               MqMessageFacade mqMessageFacade) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysRoleMenuMapper = sysRoleMenuMapper;
        this.sysMenuMapper = sysMenuMapper;
        this.sysDeptMapper = sysDeptMapper;
        this.jwtUtil = jwtUtil;
        this.mqMessageFacade = mqMessageFacade;
    }

    private String md5Encrypt(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 encryption failed", e);
        }
    }

    @Override
    public CommonResult<SysLoginResponseDTO> login(SysLoginDTO loginDTO) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, loginDTO.getUsername())
                        .eq(SysUser::getStatus, 1)
        );

        if (user == null) {
            return CommonResult.fail("用户不存在或已禁用");
        }

        String encryptedPassword = md5Encrypt(loginDTO.getPassword());
        if (!encryptedPassword.equals(user.getPassword())) {
            return CommonResult.fail("密码错误");
        }

        UserInfo userInfo = buildUserInfo(user);

        String token = jwtUtil.generateToken(userInfo);
        String refreshToken = jwtUtil.generateRefreshToken(userInfo);

        SysLoginResponseDTO response = new SysLoginResponseDTO();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(jwtUtil.getExpire());
        
        // 构建完整的用户信息
        SysUserDTO userDTO = BeanUtils.convert(user, SysUserDTO.class);
        userDTO.setTenantId(userInfo.getTenantId() != null ? Long.parseLong(userInfo.getTenantId()) : null);
        userDTO.setShopId(userInfo.getShopId() != null ? Long.parseLong(userInfo.getShopId()) : null);
        userDTO.setWarehouseId(userInfo.getWarehouseId() != null ? Long.parseLong(userInfo.getWarehouseId()) : null);
        userDTO.setRoleId(userInfo.getRoleId() != null ? Long.parseLong(userInfo.getRoleId()) : null);
        userDTO.setRoleName(userInfo.getRoleName());
        userDTO.setPermissions(userInfo.getPermissions());
        response.setUserInfo(userDTO);

        // 使用 MqMessageFacade 异步发送登录成功日志
        sendLoginSuccessLogAsync(user);

        return CommonResult.success("登录成功", response);
    }

    /**
     * 使用 MqMessageFacade 异步发送登录成功日志
     *
     * @param user 用户实体
     */
    private void sendLoginSuccessLogAsync(SysUser user) {
        try {
            // 构建登录日志消息DTO
            SysLoginLogMessageDTO logMessage = SysLoginLogMessageDTO.builder()
                    .tenantId(user.getTenantId()+"")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .loginType("PASSWORD")
                    .loginTime(LocalDateTime.now())
                    .ipAddress(getClientIp())
                    .userAgent(getUserAgent())
                    .success(1)
                    .errorMessage(null)
                    .build();

            // 使用 MessageFactory 创建 MqCommonMessage
            MqCommonMessage<SysLoginLogMessageDTO> message = MessageFactory.create(
                    logMessage,
                    RabbitMQConstant.LOGIN_LOG_EXCHANGE,
                    RabbitMQConstant.LOGIN_LOG_ROUTING_KEY,
                    "LOGIN_SUCCESS"
            );

            // 使用 MqMessageFacade 异步发送
            mqMessageFacade.sendAsync(message);
        } catch (Exception e) {
            // MQ发送失败不影响登录流程，仅记录日志
            e.printStackTrace();
        }
    }

    /**
     * 获取客户端真实IP地址
     * 
     * <p>支持通过代理服务器（如Nginx、负载均衡器）获取真实客户端IP
     * 按优先级检查以下请求头：
     * <ol>
     *   <li>X-Forwarded-For</li>
     *   <li>X-Real-IP</li>
     *   <li>Proxy-Client-IP</li>
     *   <li>WL-Proxy-Client-IP</li>
     *   <li>HTTP_CLIENT_IP</li>
     *   <li>HTTP_X_FORWARDED_FOR</li>
     * </ol>
     *
     * @return 客户端IP地址，获取失败返回 "unknown"
     */
    private String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return "unknown";
        }

        // 定义需要检查的请求头顺序
        List<String> headerNames = Arrays.asList(
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        );

        String ip = null;
        for (String header : headerNames) {
            ip = request.getHeader(header);
            if (isValidIp(ip)) {
                break;
            }
        }

        // 如果通过请求头没有获取到IP，则直接获取远程地址
        if (!isValidIp(ip)) {
            ip = request.getRemoteAddr();
        }

        // X-Forwarded-For 可能包含多个IP，取第一个
        if (StringUtils.hasText(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        // 处理IPv6本地地址
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }

        return StringUtils.hasText(ip) ? ip : "unknown";
    }

    /**
     * 获取用户代理（浏览器信息）
     *
     * @return 用户代理字符串，获取失败返回 "unknown"
     */
    private String getUserAgent() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return "unknown";
        }
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.hasText(userAgent) ? userAgent : "unknown";
    }

    /**
     * 获取当前HTTP请求对象
     *
     * @return HttpServletRequest 对象，非Web环境返回 null
     */
    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证IP地址是否有效
     *
     * @param ip IP地址字符串
     * @return true表示有效，false表示无效
     */
    private boolean isValidIp(String ip) {
        return StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip);
    }

    @Override
    public CommonResult<Void> logout() {
        UserContext.clearAll();
        return CommonResult.success("退出成功", null);
    }

    @Override
    public CommonResult<SysLoginResponseDTO> refreshToken(String refreshToken) {
        try {
            UserInfo userInfo = jwtUtil.getUserInfoFromToken(refreshToken);

            SysUser user = sysUserMapper.selectById(Long.parseLong(userInfo.getUpdateUserId()));
            if (user == null || user.getStatus() != 1) {
                return CommonResult.fail("用户不存在或已禁用");
            }

            userInfo = buildUserInfo(user);
            UserContext.set(userInfo);

            String newToken = jwtUtil.generateToken(userInfo);
            String newRefreshToken = jwtUtil.generateRefreshToken(userInfo);

            SysLoginResponseDTO response = new SysLoginResponseDTO();
            response.setToken(newToken);
            response.setRefreshToken(newRefreshToken);
            response.setExpiresIn(jwtUtil.getExpire());
            
            // 构建完整的用户信息
            SysUserDTO userDTO = BeanUtils.convert(user, SysUserDTO.class);
            userDTO.setTenantId(userInfo.getTenantId() != null ? Long.parseLong(userInfo.getTenantId()) : null);
            userDTO.setShopId(userInfo.getShopId() != null ? Long.parseLong(userInfo.getShopId()) : null);
            userDTO.setWarehouseId(userInfo.getWarehouseId() != null ? Long.parseLong(userInfo.getWarehouseId()) : null);
            userDTO.setRoleId(userInfo.getRoleId() != null ? Long.parseLong(userInfo.getRoleId()) : null);
            userDTO.setRoleName(userInfo.getRoleName());
            userDTO.setPermissions(userInfo.getPermissions());
            response.setUserInfo(userDTO);

            return CommonResult.success("token刷新成功", response);
        } catch (Exception e) {
            return CommonResult.fail("refresh token无效");
        }
    }

    private UserInfo buildUserInfo(SysUser user) {
        UserInfo userInfo = new UserInfo();
        // 处理租户ID：如果为空或无效，使用默认值1
        Long tenantId = user.getTenantId();
        String tenantIdStr = (tenantId != null && tenantId > 0) ? String.valueOf(tenantId) : "1";
        userInfo.setTenantId(tenantIdStr);
        userInfo.setUpdateUserId(String.valueOf(user.getId()));
        userInfo.setUpdateUserName(user.getNickname() != null && !user.getNickname().isEmpty() ? user.getNickname() : user.getUsername());

        Long shopId = null;
        Long warehouseId = null;

        if (user.getDeptId() != null) {
            SysDept dept = sysDeptMapper.selectById(user.getDeptId());
            if (dept != null && dept.getShopId() != null) {
                shopId = dept.getShopId();
            }
        }

        userInfo.setShopId(shopId != null ? String.valueOf(shopId) : null);
        userInfo.setWarehouseId(warehouseId != null ? String.valueOf(warehouseId) : null);

        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, user.getId())
        );

        if (!userRoles.isEmpty()) {
            SysRole role = sysRoleMapper.selectById(userRoles.get(0).getRoleId());
            if (role != null) {
                userInfo.setRoleId(String.valueOf(role.getId()));
                userInfo.setRoleCode(role.getRoleCode());
                userInfo.setRoleName(role.getRoleName());
            }

            List<com.psi.system.entity.SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                    new LambdaQueryWrapper<SysRoleMenu>()
                            .eq(SysRoleMenu::getRoleId, userRoles.get(0).getRoleId())
            );
            if (roleMenus != null && !roleMenus.isEmpty()) {
                StringBuilder permissions = new StringBuilder();
                for (com.psi.system.entity.SysRoleMenu roleMenu : roleMenus) {
                    com.psi.system.entity.SysMenu menu = sysMenuMapper.selectById(roleMenu.getMenuId());
                    if (menu != null && menu.getPermissionCode() != null) {
                        if (permissions.length() > 0) {
                            permissions.append(",");
                        }
                        permissions.append(menu.getPermissionCode());
                    }
                }
                userInfo.setPermissions(permissions.toString());
            }
        }

        return userInfo;
    }
}