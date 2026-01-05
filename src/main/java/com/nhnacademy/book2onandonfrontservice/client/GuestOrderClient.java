package com.nhnacademy.book2onandonfrontservice.client;// [Front Server] com.nhnacademy.front.adaptor.OrderAdaptor (또는 Client)

import com.nhnacademy.book2onandonfrontservice.dto.orderDto.GuestLoginRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.GuestLoginResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.guest.GuestOrderCreateRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderPrepareRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderCreateResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderPrepareResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "gateway-service", contextId = "GuestOrderClient", url = "${gateway.base-url}")
public interface GuestOrderClient {

    String GUEST_ID_HEADER = "X-Guest-Id";

    @PostMapping("/api/guest/orders/login")
    ResponseEntity<GuestLoginResponseDto> loginGuest(@RequestBody GuestLoginRequestDto requestDto);

    @PostMapping("/api/guest/orders/prepare")
    OrderPrepareResponseDto getOrderPrepare(
            @RequestHeader(GUEST_ID_HEADER) String guestId,
            @RequestBody OrderPrepareRequestDto requestDto);

    @PostMapping("/api/guest/orders")
    OrderCreateResponseDto createGuestOrder(@RequestHeader(GUEST_ID_HEADER) String guestId,
                                            @RequestBody GuestOrderCreateRequestDto req);

    @PatchMapping("/api/guest/orders/{orderNumber}/cancel")
    void cancelOrder(@PathVariable("orderNumber") String orderNumber,
                     @RequestHeader(value = "X-Guest-Order-Token", required = false) String guestToken);
}