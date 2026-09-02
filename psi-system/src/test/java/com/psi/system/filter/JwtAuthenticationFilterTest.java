package com.psi.system.filter;

import com.psi.common.context.UserContext;
import com.psi.system.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JWT 认证过滤器单元测试
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtUtil);
        ReflectionTestUtils.setField(filter, "activeProfile", "prod");
        ReflectionTestUtils.setField(filter, "ignoreUrls", java.util.List.of("/psi/admin/login", "/psi/admin/refresh-token"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        UserContext.clearAll();
    }

    @Test
    void doFilter_shouldSkipLoginUrl() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/psi/admin/login");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void doFilter_shouldSkipActuatorUrl() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/actuator/health");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldRejectMissingToken() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/psi/admin/users");
        when(request.getHeader("Authorization")).thenReturn(null);

        StringWriter writer = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(writer));

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
        assertTrue(writer.toString().contains("请先登录"));
    }

    @Test
    void doFilter_shouldRejectInvalidToken() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/psi/admin/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);

        StringWriter writer = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(writer));

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(writer.toString().contains("Token无效"));
    }

    @Test
    void doFilter_shouldSetAuthenticationWithValidToken() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/psi/admin/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);

        com.psi.common.context.UserInfo tokenUserInfo = new com.psi.common.context.UserInfo();
        tokenUserInfo.setTenantId("1");
        tokenUserInfo.setUpdateUserId("100");
        tokenUserInfo.setUpdateUserName("test");
        tokenUserInfo.setRoleCode("ADMIN");
        tokenUserInfo.setPermissions("user:read,user:write");
        when(jwtUtil.getUserInfoFromToken("valid-token")).thenReturn(tokenUserInfo);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(any(), eq(response));
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("100", authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("user:read")));
    }
}
