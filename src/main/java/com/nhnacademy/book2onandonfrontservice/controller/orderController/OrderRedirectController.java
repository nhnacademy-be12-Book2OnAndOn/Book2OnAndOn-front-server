package com.nhnacademy.book2onandonfrontservice.controller.orderController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderRedirectController {

    @GetMapping("/orders/me")
    public String redirectLegacyOrders() {
        return "redirect:/users/me/orders";
    }

    @GetMapping("/orders/guest")
    public String guestOrderLookup() {
        // 로그인 없이 접근 가능한 비회원 주문/배송 조회 화면
        return "orderpayment/OrderHistoryGuest";
    }
}
