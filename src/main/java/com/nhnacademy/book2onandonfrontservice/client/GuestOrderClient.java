package com.nhnacademy.book2onandonfrontservice.client;// [Front Server] com.nhnacademy.front.adaptor.OrderAdaptor (또는 Client)

import com.nhnacademy.book2onandonfrontservice.dto.orderDto.GuestLoginRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.GuestLoginResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "gateway-service", contextId = "GuestOrderClient", url = "${gateway.base-url}")
public interface GuestOrderClient {

    @PostMapping("/api/orders/guest/login")
    ResponseEntity<GuestLoginResponseDto> loginGuest(@RequestBody GuestLoginRequestDto requestDto);
}