package com.nhnacademy.book2onandonfrontservice.controller.orderController;

import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderRedirectController {

    @GetMapping("/users/me/orders/view")
    public String redirectLegacyOrders() {
        return "redirect:/orders/history";
    }

    @GetMapping("/orders/guest")
    public String guestOrderLookup() {
        // 로그인 없이 접근 가능한 비회원 주문/배송 조회 화면
        return "orderpayment/OrderHistoryGuest";
    }

    @GetMapping("/orders/payment")
    public String orderPaymentPage() {
        // 결제/주문 작성 화면
        return "orderpayment/OrderPayment";
    }

    @GetMapping("/orders/history")
    public String orderHistoryPage(HttpServletRequest request) {
        // 로그인 필요: 비로그인은 로그인 페이지로 유도
        String accessToken = CookieUtils.getCookieValue(request, "accessToken");
        if (accessToken == null || accessToken.isBlank()) {
            return "redirect:/login";
        }
        return "orderpayment/OrderHistory";
    }
}
