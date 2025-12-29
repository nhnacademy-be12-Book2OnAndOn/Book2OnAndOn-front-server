package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.paymentDto.request.CommonConfirmRequest;
import com.nhnacademy.book2onandonfrontservice.dto.paymentDto.response.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "gateway-service", contextId = "paymentClient", url = "${gateway.base-url}")
public interface PaymentClient {
    @PostMapping("/api/payment/{provider}/confirm")
    PaymentResponse confirmPayment(@RequestHeader("Authorization")String accessToken,
                                   @PathVariable("provider")String provider,
                                   @RequestBody CommonConfirmRequest req);
}
