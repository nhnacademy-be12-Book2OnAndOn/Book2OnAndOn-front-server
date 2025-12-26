package com.nhnacademy.book2onandonfrontservice.controller.orderController;

import com.nhnacademy.book2onandonfrontservice.client.OrderClient;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.GuestLookupRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.OrderActionRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.OrderDetailResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.OrderListResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.OrderRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.OrderResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderClientController {

    private final OrderClient orderClient;

    // 1. 주문 생성
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestHeader(value = "X-Guest-Id", required = false) String uuid,
            @Valid @RequestBody OrderRequestDto requestDto) {
        String authHeader = (accessToken != null) ? "Bearer " + accessToken : null;
        return ResponseEntity.ok(orderClient.createOrder(authHeader, uuid, requestDto));
    }

    // 2. 주문 목록 조회 (회원)
    @GetMapping("/me")
    public ResponseEntity<OrderListResponseDto> getMemberOrders(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam Map<String, String> params) {
        return ResponseEntity.ok(orderClient.getMemberOrders("Bearer " + accessToken,
                params.get("year"),
                params.get("month"),
                params.get("status")));
    }

    // 3. 주문 목록 조회 (비회원)
    @PostMapping("/guest/lookup")
    public ResponseEntity<OrderDetailResponseDto> lookupGuestOrder(
            @Valid @RequestBody GuestLookupRequestDto lookupDto) {

        // OrderClient 통해 백엔드(Gateway)에 조회 요청
        // 백엔드에서는 DB에서 주문번호+이름+비밀번호가 일치하는지 확인 후 상세 정보를 반환
        OrderDetailResponseDto response = orderClient.lookupGuestOrder(lookupDto);

        return ResponseEntity.ok(response);
    }

    // 4. 주문 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponseDto> getOrderDetail(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @PathVariable String orderId) {
        String authHeader = (accessToken != null) ? "Bearer " + accessToken : null;
        return ResponseEntity.ok(orderClient.getOrderDetail(authHeader, orderId));
    }

    // 5. 주문 취소/반품
    @PostMapping("/{orderId}/{action}")
    public ResponseEntity<Void> handleOrderAction(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @PathVariable String orderId,
            @PathVariable String action,
            @Valid @RequestBody OrderActionRequestDto actionDto) {
        String authHeader = (accessToken != null) ? "Bearer " + accessToken : null;
        orderClient.handleOrderAction(authHeader, orderId, action, actionDto);
        return ResponseEntity.ok().build();
    }
}