package com.nhnacademy.book2onandonfrontservice.controller.orderController;

import com.nhnacademy.book2onandonfrontservice.client.GuestOrderClient;
import com.nhnacademy.book2onandonfrontservice.client.OrderUserClient;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderDetailResponseDto;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/guest/orders")
public class OrderGuestController {
    private final OrderUserClient orderUserClient;
    private final GuestOrderClient guestOrderClient;

    @GetMapping("/{orderNumber}")
    public String getOrderDetail(Model model,
                                 @CookieValue(value = "guestOrderToken", required = false) String guestToken,
                                 @PathVariable("orderNumber") String orderNumber){
        log.info("GET /guest/orders/{} 호출 : 주문 상세 데이터 반환" , orderNumber);

        if(guestToken == null){
            return "redirect:/orders/guest/login";
        }

        OrderDetailResponseDto order = orderUserClient.getOrderDetail(null, guestToken, orderNumber);

        model.addAttribute("order", order);

        return "orderpayment/guest-order-detail";
    }


    @GetMapping("/{orderNumber}/cancel")
    public String cancelGuestOrder(@CookieValue(value = "guestOrderToken", required = false) String guestToken,
                                   @PathVariable("orderNumber") String orderNumber){
        log.info("GET /guest/orders/{}/cancel : 비회원 주문 취소 요청", orderNumber);

        if(guestToken == null){
            return "redirect:/orders/guest/login";
        }

        guestOrderClient.cancelOrder(orderNumber, guestToken);

        return "redirect:/guest/orders/" + orderNumber;
    }
}
