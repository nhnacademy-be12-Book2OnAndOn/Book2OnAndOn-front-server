package com.nhnacademy.book2onandonfrontservice.controller.orderController;

import com.nhnacademy.book2onandonfrontservice.client.GuestOrderClient;
import com.nhnacademy.book2onandonfrontservice.client.OrderUserClient;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.GuestLoginRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.GuestLoginResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.guest.GuestOrderCreateRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderCreateRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderPrepareRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.BookOrderResponse;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderCreateResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderPrepareResponseDto;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
@RequiredArgsConstructor
public class OrderRedirectController {

    private final GuestOrderClient guestOrderClient;
    private final OrderUserClient orderUserClient;

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
                String orderNumber = responseBody.getOrderNumber();

                // 토큰을 쿠키에 저장
                Cookie cookie = new Cookie("guestOrderToken", accessToken);
                cookie.setPath("/");
                cookie.setMaxAge(60 * 60);
                // cookie.setHttpOnly(true); // 보안상 JavaScript 접근 막으려면 설정
                response.addCookie(cookie);

                // 주문 상세 페이지로 리다이렉트
                return "redirect:/guest/orders/" + orderNumber;
            }

        } catch (Exception e) {
            log.error("비회원 로그인 실패", e);

            return "redirect:/orders/guest/login?error=true";
        }

        return "redirect:/orders/guest/login?error=unknown";
    }

    @GetMapping("/orders/payment")
    public String orderPaymentPage(Model model) {
        // 결제/주문 작성 화면
        model.addAttribute("isGuest", false);
        return "orderpayment/OrderPayment";
    }

    @PostMapping("/guest/orders/prepare")
    public String guestOrderPaymentPage(Model model,
                                        @CookieValue(name = "GUEST_ID") String guestId,
                                        @ModelAttribute OrderPrepareRequestDto req) {

        OrderPrepareResponseDto orderPrepareResponseDto = guestOrderClient.getOrderPrepare(guestId, req);

        // 비회원 결제도 공통 템플릿 사용
        model.addAttribute("orderItems", orderPrepareResponseDto.orderItems());

        Long itemTotal = orderPrepareResponseDto.orderItems().stream()
                        .mapToLong(BookOrderResponse::getPriceSales)
                        .sum();

        model.addAttribute("itemTotal", itemTotal);
        model.addAttribute("isGuest", true);
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

    @PostMapping("/guest/orders/")
    public ResponseEntity<OrderCreateResponseDto> createPreOrder(
            @CookieValue(name = "GUEST_ID", required = false) String guestId,
            @RequestBody GuestOrderCreateRequestDto req) {
        log.info("POST /guest/orders 호출 : 사전 주문 데이터 생성");

        OrderCreateResponseDto orderCreateResponseDto = guestOrderClient.createGuestOrder(guestId, req);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderCreateResponseDto);
    }

}
