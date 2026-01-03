package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderCreateRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderPrepareRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderCancelResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderCreateResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderDetailResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderPrepareResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderSimpleDto;
import com.nhnacademy.book2onandonfrontservice.client.OrderClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "gateway-service", contextId = "OrderUserClient", url = "${gateway.base-url}")
public interface OrderUserClient {

    String GUEST_ID_HEADER = "X-Guest-Id";
    /**
     * 장바구니 혹은 바로구매시 준비할 데이터 (책 정보, 회원 배송지 정보, 회원 사용 가능한 쿠폰, 회원 현재 포인트)
     * POST /api/orders/prepare
     */
    @PostMapping("/api/orders/prepare")
    OrderPrepareResponseDto getOrderPrepare(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestBody OrderPrepareRequestDto requestDto);

    /**
     * 주문 생성(결제 직전 사전 주문데이터 생성)
     * POST /api/orders
     */
    @PostMapping("/api/orders")
    OrderCreateResponseDto createPreOrder(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestBody OrderCreateRequestDto requestDto);

    /**
     * 내 주문 목록 조회
     * GET /api/orders/my-order
     * Pageable은 쿼리 파라미터 (?page=0&size=20)로 변환되어 전달
     */
    @GetMapping("/api/orders/my-order")
    java.util.Map<String, Object> getOrderList(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            Pageable pageable);

    /**
     * 주문 상세 조회
     * GET /api/orders/{orderNumber}
     */

    @GetMapping("/api/orders/{orderNumber}")
    OrderDetailResponseDto getOrderDetail(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestHeader(value = "X-Guest-Order-Token", required = false) String guestToken,
            @PathVariable("orderNumber") String orderNumber
    );

    /**
     * 결제 후 바로 주문 취소하는 경우
     * 백엔드가 204 no Content를 보내더라도 Dto를 리턴 타입으로 두면 null 들어옴 만약 백엔드에서 200을
     */
    @PatchMapping("/api/orders/{orderNumber}/cancel")
    void cancelOrder(@RequestHeader(value = "Authorization", required = false) String accessToken,
                     @RequestHeader(value = "X-Guest-Order-Token", required = false) String guestToken,
                     @PathVariable("orderNumber") String orderNumber);
}
