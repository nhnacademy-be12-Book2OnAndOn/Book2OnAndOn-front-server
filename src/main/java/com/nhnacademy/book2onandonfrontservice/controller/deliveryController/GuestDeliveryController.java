package com.nhnacademy.book2onandonfrontservice.controller.deliveryController;

import com.nhnacademy.book2onandonfrontservice.client.DeliveryClient;
import com.nhnacademy.book2onandonfrontservice.dto.deliveryDto.DeliveryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/guest/delivery")
@RequiredArgsConstructor
public class GuestDeliveryController {

    private final DeliveryClient deliveryClient;

    @GetMapping()
    public String trackDelivery(
            @RequestParam Long orderId,
            @CookieValue(name = "guestOrderToken", required = false) String guestToken, // 쿠키 자동 추출
            Model model
    ) {
        // 비회원 토큰이 없으면 로그인(주문조회) 페이지로 튕겨내기
        if (guestToken == null) {
            return "redirect:/guest/login";
        }

        // 비회원은 accessToken 필요 없음
        DeliveryDto delivery = deliveryClient.getDeliveryByOrder(orderId, null, guestToken);

        model.addAttribute("delivery", delivery);

        // 비회원 전용 뷰 (사이드바 없음)
        return "user/guest/deliveryTrack";
    }
}