package com.psi.system.filter;

import com.psi.system.util.JwtUtil;
import com.psi.common.context.UserContext;
import com.psi.common.context.UserInfo;
import com.psi.common.constant.TenantMdcConstant;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;

    @Value("${jwt.ignore-urls:/psi/admin/login,/psi/admin/refresh-token}")
    private List<String> ignoreUrls;
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        logger.info("JwtAuthenticationFilter initialized with JwtUtil: {}", jwtUtil != null ? "OK" : "NULL");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        logger.debug("JWT filter processing URI: {}", requestURI);
        
        // Skip JWT validation for login and actuator endpoints
        if (requestURI.equals("/psi/admin/login") || 
            requestURI.equals("/psi/admin/refresh-token") ||
            requestURI.startsWith("/actuator/") ||
            requestURI.equals("/error")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Dev mode fallback: use a default super-admin authentication so that
        // method security (@PreAuthorize) can still be evaluated without a real token.
        if ("dev".equalsIgnoreCase(activeProfile)) {
            logger.debug("Development mode: using default super-admin authentication for URI: {}", requestURI);
            setDefaultDevAuthentication();
            HeaderMapRequestWrapper wrappedRequest = new HeaderMapRequestWrapper(httpRequest);
            wrappedRequest.setHeaderIfNotEmpty(TenantMdcConstant.HEADER_TENANT_ID, "1");
            wrappedRequest.setHeaderIfNotEmpty("X-User-Id", "1");
            wrappedRequest.setHeaderIfNotEmpty("X-User-Name", "dev");
            wrappedRequest.setHeaderIfNotEmpty(TenantMdcConstant.HEADER_ROLE_ID, "1");
            wrappedRequest.setHeaderIfNotEmpty(TenantMdcConstant.HEADER_ROLE_CODE, "SUPER_ADMIN");
            wrappedRequest.setHeaderIfNotEmpty(TenantMdcConstant.HEADER_ROLE_NAME, "超级管理员");
            try {
                filterChain.doFilter(wrappedRequest, response);
            } finally {
                UserContext.clearAll();
                SecurityContextHolder.clearContext();
            }
            return;
        }
        
        // Check ignore URLs
        if (ignoreUrls != null && ignoreUrls.stream().anyMatch(requestURI::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = httpRequest.getHeader("Authorization");
        logger.debug("Authorization header found: {}", StringUtils.hasText(authorizationHeader));
        
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            
            try {
                if (!jwtUtil.validateToken(token)) {
                    logger.warn("Token validation failed for URI: {}", requestURI);
                    sendUnauthorizedResponse(httpResponse, "Token无效");
                    return;
                }
                
                UserInfo headerUserInfo = new UserInfo();
                headerUserInfo.setTenantId(httpRequest.getHeader(TenantMdcConstant.HEADER_TENANT_ID));
                headerUserInfo.setShopId(httpRequest.getHeader(TenantMdcConstant.HEADER_SHOP_ID));
                headerUserInfo.setWarehouseId(httpRequest.getHeader(TenantMdcConstant.HEADER_WAREHOUSE_ID));
                headerUserInfo.setUpdateUserId(httpRequest.getHeader("X-User-Id"));
                headerUserInfo.setUpdateUserName(httpRequest.getHeader("X-User-Name"));
                headerUserInfo.setRoleId(httpRequest.getHeader(TenantMdcConstant.HEADER_ROLE_ID));
                headerUserInfo.setRoleCode(httpRequest.getHeader(TenantMdcConstant.HEADER_ROLE_CODE));
                headerUserInfo.setRoleName(httpRequest.getHeader(TenantMdcConstant.HEADER_ROLE_NAME));
                headerUserInfo.setPermissions(httpRequest.getHeader(TenantMdcConstant.HEADER_PERMISSIONS));
                
                UserInfo tokenUserInfo = jwtUtil.getUserInfoFromToken(token);
                
                UserInfo userInfo = new UserInfo();
                userInfo.setTenantId(StringUtils.hasText(headerUserInfo.getTenantId()) ? headerUserInfo.getTenantId() : tokenUserInfo.getTenantId());
                userInfo.setShopId(StringUtils.hasText(headerUserInfo.getShopId()) ? headerUserInfo.getShopId() : tokenUserInfo.getShopId());
                userInfo.setWarehouseId(StringUtils.hasText(headerUserInfo.getWarehouseId()) ? headerUserInfo.getWarehouseId() : tokenUserInfo.getWarehouseId());
                userInfo.setUpdateUserId(StringUtils.hasText(headerUserInfo.getUpdateUserId()) ? headerUserInfo.getUpdateUserId() : tokenUserInfo.getUpdateUserId());
                userInfo.setUpdateUserName(StringUtils.hasText(headerUserInfo.getUpdateUserName()) ? headerUserInfo.getUpdateUserName() : tokenUserInfo.getUpdateUserName());
                userInfo.setRoleId(StringUtils.hasText(headerUserInfo.getRoleId()) ? headerUserInfo.getRoleId() : tokenUserInfo.getRoleId());
                userInfo.setRoleCode(StringUtils.hasText(headerUserInfo.getRoleCode()) ? headerUserInfo.getRoleCode() : tokenUserInfo.getRoleCode());
                userInfo.setRoleName(StringUtils.hasText(headerUserInfo.getRoleName()) ? headerUserInfo.getRoleName() : tokenUserInfo.getRoleName());
                userInfo.setPermissions(StringUtils.hasText(headerUserInfo.getPermissions()) ? headerUserInfo.getPermissions() : tokenUserInfo.getPermissions());
                
                UserContext.set(userInfo);
                
                request.setAttribute("userInfo", userInfo);
                request.setAttribute("tenantId", userInfo.getTenantId());
                request.setAttribute("userId", userInfo.getUpdateUserId());
                
                List<GrantedAuthority> authorities = buildAuthorities(userInfo);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userInfo.getUpdateUserId(),
                        null,
                        authorities
                );
                authToken.setDetails(userInfo);
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                HeaderMapRequestWrapper wrappedRequest = new HeaderMapRequestWrapper(httpRequest);
                wrappedRequest.setHeaderIfNotEmpty(TenantMdcConstant.HEADER_TENANT_ID, userInfo.getTenantId());
                wrappedRequest.setHeaderIfNotEmpty(TenantMdcConstant.HEADER_SHOP_ID, userInfo.getShopId());
                wrappedRequest.setHeaderIfNotEmpty(TenantMdcConstant.HEADER_WAREHOUSE_ID, userInfo.getWarehouseId());
                wrappedRequest.setHeaderIfNotEmpty("X-User-Id", userInfo.getUpdateUserId());
                wrappedRequest.setHeaderIfNotEmpty("X-User-Name", userInfo.getUpdateUserName());
                wrappedRequest.setHeaderIfNotEmpty(TenantMdcConstant.HEADER_ROLE_ID, userInfo.getRoleId());
                wrappedRequest.setHeaderIfNotEmpty(TenantMdcConstant.HEADER_ROLE_CODE, userInfo.getRoleCode());
                wrappedRequest.setHeaderIfNotEmpty(TenantMdcConstant.HEADER_ROLE_NAME, userInfo.getRoleName());
                wrappedRequest.setHeaderIfNotEmpty(TenantMdcConstant.HEADER_PERMISSIONS, userInfo.getPermissions());
                
                try {
                    filterChain.doFilter(wrappedRequest, response);
                } finally {
                    UserContext.clearAll();
                }
                return;
            } catch (Exception e) {
                logger.warn("Token processing failed: {}", e.getMessage());
                sendUnauthorizedResponse(httpResponse, "Token无效或已过期");
                return;
            }
        } else {
            logger.warn("Missing or invalid authorization header for URI: {}", requestURI);
            sendUnauthorizedResponse(httpResponse, "请先登录");
            return;
        }
    }

    private void setDefaultDevAuthentication() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                "1", null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authToken);

        UserInfo userInfo = new UserInfo();
        userInfo.setTenantId("1");
        userInfo.setUpdateUserId("1");
        userInfo.setUpdateUserName("dev");
        userInfo.setRoleId("1");
        userInfo.setRoleCode("SUPER_ADMIN");
        userInfo.setRoleName("超级管理员");
        UserContext.set(userInfo);
    }

    private List<GrantedAuthority> buildAuthorities(UserInfo userInfo) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (StringUtils.hasText(userInfo.getRoleCode())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + userInfo.getRoleCode()));
        } else if (StringUtils.hasText(userInfo.getRoleId())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + userInfo.getRoleId()));
        }
        if (StringUtils.hasText(userInfo.getPermissions())) {
            Arrays.stream(userInfo.getPermissions().split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }
        return authorities;
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
    }

    @Override
    public void destroy() {
    }

    private static class HeaderMapRequestWrapper extends HttpServletRequestWrapper {
        private Map<String, String> headerMap = new HashMap<>();

        public HeaderMapRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        public void addHeader(String name, String value) {
            headerMap.put(name, value);
        }

        public void setHeaderIfNotEmpty(String name, String value) {
            if (StringUtils.hasText(value)) {
                headerMap.put(name, value);
            }
        }

        @Override
        public String getHeader(String name) {
            String headerValue = ((HttpServletRequest) getRequest()).getHeader(name);
            if (headerMap.containsKey(name)) {
                headerValue = headerMap.get(name);
            }
            return headerValue;
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            List<String> values = Collections.list(((HttpServletRequest) getRequest()).getHeaders(name));
            if (headerMap.containsKey(name)) {
                values.add(headerMap.get(name));
            }
            return Collections.enumeration(values);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Enumeration<String> originalNames = ((HttpServletRequest) getRequest()).getHeaderNames();
            List<String> names = originalNames != null ? Collections.list(originalNames) : new java.util.ArrayList<>();
            names.addAll(headerMap.keySet());
            return Collections.enumeration(names);
        }
    }
}