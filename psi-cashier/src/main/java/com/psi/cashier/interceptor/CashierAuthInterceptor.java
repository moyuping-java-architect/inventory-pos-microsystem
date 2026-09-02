package com.psi.cashier.interceptor;

import com.psi.cashier.service.CashierAuthService;
import com.psi.common.result.CommonResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

/**
 * 收银认证拦截器
 * 保护需要登录的接口
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CashierAuthInterceptor implements HandlerInterceptor {

    private final CashierAuthService cashierAuthService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // 允许 OPTIONS 请求通过（CORS 预检请求）
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        
        // 允许登录接口直接访问
        if (uri.contains("/psi/cashier/auth/login")) {
            return true;
        }

        // 允许静态资源访问
        if (uri.endsWith(".html") || uri.endsWith(".js") || uri.endsWith(".css") || uri.endsWith(".ico")) {
            return true;
        }

        // 获取Token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write(objectMapper.writeValueAsString(CommonResult.fail("请先登录")));
            writer.flush();
            return false;
        }

        // 验证Token
        boolean valid = cashierAuthService.validateToken(token);
        if (!valid) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write(objectMapper.writeValueAsString(CommonResult.fail("登录已失效，请重新登录")));
            writer.flush();
            return false;
        }

        return true;
    }
}