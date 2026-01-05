package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundAvailableItemResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundResponseDto;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "gateway-service", contextId = "RefundClient", url = "${gateway.base-url}")
public interface RefundClient {

    // 반품 신청 폼
    @GetMapping("/api/orders/{orderId}/refunds/form")
    List<RefundAvailableItemResponseDto> getRefundForm(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestHeader(value = "X-Guest-Order-Token", required = false) String guestOrderToken,
            @PathVariable("orderId") Long orderId
    );

    // 반품 신청
    @PostMapping("/api/orders/{orderId}/refunds")
    RefundResponseDto createRefund(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestHeader(value = "X-Guest-Order-Token", required = false) String guestOrderToken,
            @PathVariable("orderId") Long orderId,
            @RequestBody RefundRequestDto requestDto
    );

    // 번퓸 신청 취소
    @PostMapping("/api/orders/{orderId}/refunds/{refundId}/cancel")
    RefundResponseDto cancelRefund(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestHeader(value = "X-Guest-Order-Token", required = false) String guestOrderToken,
            @PathVariable("orderId") Long orderId,
            @PathVariable("refundId") Long refundId
    );

    // 반품 상세 조회
    @GetMapping("/api/orders/{orderId}/refunds/{refundId}")
    RefundResponseDto getRefundDetails(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestHeader(value = "X-Guest-Order-Token", required = false) String guestOrderToken,
            @PathVariable("orderId") Long orderId,
            @PathVariable("refundId") Long refundId
    );

    // 회원 전용(게이트웨이가 X-User-Id를 붙여줌)
    // 회원용 전체 반품 목록 조회
    @GetMapping("/api/orders/refunds/my-list")
    Page<RefundResponseDto> getMyRefunds(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            Pageable pageable
    );
}

