package com.nhnacademy.book2onandonfrontservice.controller.deliveryController;


import com.nhnacademy.book2onandonfrontservice.client.DeliveryClient;
import com.nhnacademy.book2onandonfrontservice.dto.deliveryDto.DeliveryDto;
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
    public String trackDelivery(@RequestParam Long orderId, Model model) {

        DeliveryDto delivery = deliveryClient.getDeliveryByOrder(orderId);

        model.addAttribute("delivery", delivery);

        return "user/mypage/deliveryTrack";
    }
}