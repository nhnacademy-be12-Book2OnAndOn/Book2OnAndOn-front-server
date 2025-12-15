package com.nhnacademy.book2onandonfrontservice.interceptor;

import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import com.nhnacademy.book2onandonfrontservice.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // 토큰 확인
        String token = CookieUtils.getCookieValue(request, "accessToken");
        if (token == null) {
            response.sendRedirect("/login");
            return false;
        }

        // 역할 및 요청 URL 추출
        String role = JwtUtils.getRole(token);
        String requestURI = request.getRequestURI();

        // 기본 관리자 권한 체크
        if (role == null || !role.contains("ADMIN")) {
            sendAlertAndRedirect(response, "관리자 페이지 접근 권한이 없습니다.", "/");
            return false;
        }

        // SUPER_ADMIN은 모든 페이지 프리패스
        if ("ROLE_SUPER_ADMIN".equals(role)) {
            return true;
        }

        // 세부 권한별 접근 제어 (권한 없으면 메세지 띄우고 이전페이지로)
        if (!hasPermission(requestURI, role)) {
            log.warn("권한 부족 접근 시도: URI={}, Role={}", requestURI, role);
            sendAlertAndBack(response, "해당 메뉴에 대한 접근 권한이 없습니다.");
            return false;
        }

        return true;
    }

    /**
     * URL별 필요 권한 체크 로직
     */
    private boolean hasPermission(String uri, String role) {
        // [회원 관리자] : 회원, 등급, 포인트 내역
        if (uri.startsWith("/admin/users") || uri.startsWith("/admin/grades") || uri.startsWith("/admin/points")) {
            return "ROLE_MEMBER_ADMIN".equals(role);
        }

        // [도서 관리자] : 도서 관리
        if (uri.startsWith("/admin/books")) {
            return "ROLE_BOOK_ADMIN".equals(role);
        }

        // [쿠폰 관리자] : 쿠폰, 쿠폰 정책, 포인트 정책
        if (uri.startsWith("/admin/coupons") || uri.startsWith("/admin/policies") || uri.startsWith(
                "/admin/point-policies")) {
            return "ROLE_COUPON_ADMIN".equals(role);
        }

        // [주문 관리자] : 주문, 배송, 배송 정책
        if (uri.startsWith("/admin/orders") || uri.startsWith("/admin/deliveries") || uri.startsWith(
                "/admin/delivery-policies")) {
            return "ROLE_ORDER_ADMIN".equals(role);
        }

        // 그 외 페이지(대시보드 /admin 등)는 모든 관리자가 접근 가능
        return true;
    }

    /**
     * 알림창을 띄우고 이전 페이지로 이동시키는 메서드
     */
    private void sendAlertAndBack(HttpServletResponse response, String msg) throws Exception {
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<script>alert('" + msg + "'); history.back();</script>");
        out.flush();
    }

    /**
     * 알림창을 띄우고 특정 URL로 이동시키는 메서드
     */
    private void sendAlertAndRedirect(HttpServletResponse response, String msg, String url) throws Exception {
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<script>alert('" + msg + "'); location.href='" + url + "';</script>");
        out.flush();
    }
}