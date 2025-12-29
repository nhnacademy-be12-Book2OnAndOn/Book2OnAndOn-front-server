package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.orderDto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "gateway-service", contextId = "orderClient", url = "${gateway.base-url}")
public interface OrderClient {

    String GUEST_ID_HEADER = "X-Guest-Id";

    // 1. 주문 생성
    @PostMapping("/api/orders")
    OrderResponseDto createOrder(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestHeader(value = GUEST_ID_HEADER, required = false) String uuid,
            @RequestBody OrderRequestDto orderRequest
    );

    // 2. 내 주문 목록 조회
    @GetMapping("/api/orders/me")
    OrderListResponseDto getMemberOrders(
            @RequestHeader("Authorization") String accessToken,
            @RequestParam(value = "year", required = false) String year,
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "status", required = false) String status
    );

    // 4. 주문 상세 조회
    @GetMapping("/api/orders/{orderId}")
    OrderDetailResponseDto getOrderDetail(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable("orderId") String orderId
    );

    // 5. 주문 취소/반품 처리
    @PostMapping("/api/orders/{orderId}/{action}")
    void handleOrderAction(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable("orderId") String orderId,
            @PathVariable("action") String action,
            @RequestBody OrderActionRequestDto actionDto
    );
}