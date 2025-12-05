package com.nhnacademy.book2onandonfrontservice.controller.deliveryController;


import com.nhnacademy.book2onandonfrontservice.client.DeliveryClient;
import com.nhnacademy.book2onandonfrontservice.dto.deliveryDto.DeliveryDto;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryViewController {

    private final DeliveryClient deliveryClient;

    @GetMapping
    public String trackDelivery(@RequestParam Long orderId, HttpServletRequest request, Model model) {

        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");
        DeliveryDto delivery = deliveryClient.getDeliveryByOrder(orderId, token);

        model.addAttribute("delivery", delivery);

        return "user/mypage/deliveryTrack";
    }
}