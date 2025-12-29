package com.nhnacademy.book2onandonfrontservice.controller.orderController;

import com.nhnacademy.book2onandonfrontservice.client.PaymentClient;
import com.nhnacademy.book2onandonfrontservice.dto.paymentDto.request.CommonConfirmRequest;
import com.nhnacademy.book2onandonfrontservice.dto.paymentDto.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
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
    public String confirmPayment(@RequestHeader(value = "accessToken", required = false) String accessToken,
                                 @PathVariable("provider")String provider,
                                 @RequestParam("orderId") String orderId,
                                 @RequestParam("paymentKey") String paymentKey,
                                 @RequestParam("amount") Integer amount){

        log.info("GET /payment/{}/confirm 결제 승인 요청", provider);

        String token = null;
        if(accessToken != null){
            token = accessToken.startsWith("Bearer ") ? accessToken : "Bearer " + accessToken;
        }

        CommonConfirmRequest req = new CommonConfirmRequest(orderId, paymentKey, amount);

        paymentClient.confirmPayment(token, provider, req);

        return "redirect:/";
    }
}
