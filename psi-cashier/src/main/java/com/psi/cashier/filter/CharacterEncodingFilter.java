package com.psi.cashier.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/*")
public class CharacterEncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        if (response instanceof HttpServletResponse && request instanceof HttpServletRequest) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String requestURI = httpRequest.getRequestURI();
            
            if (requestURI.endsWith(".js")) {
                httpResponse.setContentType("text/javascript;charset=UTF-8");
            } else if (requestURI.endsWith(".css")) {
                httpResponse.setContentType("text/css;charset=UTF-8");
            } else if (requestURI.endsWith(".html")) {
                httpResponse.setContentType("text/html;charset=UTF-8");
            }
        }
        
        chain.doFilter(request, response);
    }
}