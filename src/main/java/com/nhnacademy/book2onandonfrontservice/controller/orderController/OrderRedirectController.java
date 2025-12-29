package com.nhnacademy.book2onandonfrontservice.controller.orderController;

import com.nhnacademy.book2onandonfrontservice.client.GuestOrderClient;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.GuestLoginRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.GuestLoginResponseDto;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
public class OrderRedirectController {

    private final GuestOrderClient guestOrderClient;

    @GetMapping("/users/me/orders/view")
    public String redirectLegacyOrders() {
        return "redirect:/orders/history";
    }

    @GetMapping("/orders/guest/login")
    public String guestOrderLookup() {
        // 로그인 없이 접근 가능한 비회원 주문/배송 조회 화면
        return "orderpayment/OrderHistoryGuest";
    }

    @PostMapping("/orders/guest/login")
    public String loginProcess(@ModelAttribute GuestLoginRequestDto requestDto,
                               HttpServletResponse response) {

        try {
            // Feign Client를 통해 백엔드(Gateway -> Order Service) 호출
            ResponseEntity<GuestLoginResponseDto> result = guestOrderClient.loginGuest(requestDto);

            GuestLoginResponseDto responseBody = result.getBody();

            if (responseBody != null) {
                String accessToken = responseBody.getAccessToken();
                Long orderId = responseBody.getOrderId();

                // 토큰을 쿠키에 저장
                Cookie cookie = new Cookie("guestOrderToken", accessToken);
                cookie.setPath("/");
                cookie.setMaxAge(60 * 60);
                // cookie.setHttpOnly(true); // 보안상 JavaScript 접근 막으려면 설정
                response.addCookie(cookie);

                // 주문 상세 페이지로 리다이렉트
                return "redirect:/orders/" + orderId;
            }

        } catch (Exception e) {
            log.error("비회원 로그인 실패", e);

            return "redirect:/orders/guest/login?error=true";
        }

        return "redirect:/orders/guest/login?error=unknown";
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
        return "redirect:/orders/my-order";
    }
}
