package com.nhnacademy.book2onandonfrontservice.controller.orderController;

import com.nhnacademy.book2onandonfrontservice.client.PaymentClient;
import com.nhnacademy.book2onandonfrontservice.dto.paymentDto.request.CommonConfirmRequest;
import com.nhnacademy.book2onandonfrontservice.dto.paymentDto.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentClient paymentClient;

    @GetMapping("/payment/{provider}/confirm")
    public String confirmPayment(@CookieValue(value = "accessToken", required = false) String accessToken,
                                 @PathVariable("provider") String provider,
                                 @RequestParam("orderId") String orderId,
                                 @RequestParam("paymentKey") String paymentKey,
                                 @RequestParam("amount") Integer amount,
                                 Model model){

        log.info("GET /payment/{}/confirm 결제 승인 요청", provider);

        CommonConfirmRequest req = new CommonConfirmRequest(orderId, paymentKey, amount);
        PaymentResponse response = paymentClient.confirmPayment(provider, req);

        if(accessToken == null){
            model.addAttribute("payment", response);
            return "orderpayment/guest-order-complete";
        }


        // 결제 성공 시 주문 완료 페이지로 이동
        return "redirect:/orders/complete/" + response.orderNumber();
    }

    @GetMapping("/payment/{provider}/fail")
    public String failPayment(Model model,
                              @PathVariable("provider") String provider,
                              @RequestParam("code") String code,
                              @RequestParam("message") String message){

        model.addAttribute("provider", provider);
        model.addAttribute("code", code);
        model.addAttribute("message", message);

        return "orderpayment/error";
    }
}
