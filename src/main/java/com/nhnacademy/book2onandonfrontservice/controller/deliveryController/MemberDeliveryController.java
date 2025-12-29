package com.nhnacademy.book2onandonfrontservice.controller.deliveryController;

import com.nhnacademy.book2onandonfrontservice.client.DeliveryClient;
import com.nhnacademy.book2onandonfrontservice.dto.deliveryDto.DeliveryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/mypage/delivery")
@RequiredArgsConstructor
public class MemberDeliveryController {

    private final DeliveryClient deliveryClient;

    @GetMapping()
    public String trackDelivery(
            @RequestParam Long orderId,
            @CookieValue(name = "accessToken", required = false) String accessToken, // 쿠키에서 추출
            Model model
    ) {

        if (!StringUtils.hasText(accessToken)) {
            return "redirect:/login";
        }

        String authorizationHeader = "Bearer " + accessToken;

        DeliveryDto delivery = deliveryClient.getDeliveryByOrder(orderId, authorizationHeader, null);

        model.addAttribute("delivery", delivery);

        return "user/mypage/deliveryTrack";
    }
}