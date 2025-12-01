package com.nhnacademy.book2onandonfrontservice.interceptor;

import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import com.nhnacademy.book2onandonfrontservice.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class AdminInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String token = CookieUtils.getCookieValue(request, "accessToken");

        if (token == null) {
            response.sendRedirect("/login");
            return false;
        }

        String role = JwtUtils.getRole(token);
        if (role == null || !role.contains("ADMIN")) {
            log.warn("관리자 페이지 접근 거부: IP={}, Role={}", request.getRemoteAddr(), role);
            response.sendRedirect("/");
            return false;
        }
        return true;
    }
}
