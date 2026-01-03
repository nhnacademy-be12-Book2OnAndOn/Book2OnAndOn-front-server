package com.nhnacademy.book2onandonfrontservice.controller.orderController;

import com.nhnacademy.book2onandonfrontservice.client.OrderUserClient;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderDetailResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/guest/order")
public class OrderGuestController {
    private final OrderUserClient orderUserClient;

    @GetMapping("/{orderNumber}")
    public String getOrderDetail(Model model,
                                 @CookieValue(value = "guestOrderToken", required = false) String guestToken,
                                 @PathVariable("orderNumber") String orderNumber){
        log.info("GET /orders/{} 호출 : 주문 상세 데이터 반환" , orderNumber);

        if(guestToken == null){
            return "redirect:/guest/login";
        }

        OrderDetailResponseDto orderResponseDto = orderUserClient.getOrderDetail(null, guestToken, orderNumber);

        model.addAttribute("orderInfo", orderResponseDto);

        // TODO 주문 상세 내역 만들기
        return "user/guest/order-detail";
    }

}
