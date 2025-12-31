package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.RefundGuestRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.RefundAvailableItemResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.RefundResponseDto;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "gateway-service", contextId = "RefundGuestClient", url = "${gateway.base-url}")
public interface RefundGuestClient {

    /**
     * 비회원 반품 신청 폼 (반품 가능 목록 조회)
     */
    @GetMapping("/api/guest/orders/{orderId}/refunds/form")
    List<RefundAvailableItemResponseDto> getRefundFormForGuest(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                                               @PathVariable("orderId") Long orderId);

    /**
     * 비회원 반품 신청
     */
    @PostMapping("/api/guest/orders/{orderId}/refunds")
    RefundResponseDto createRefundForGuest(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                           @PathVariable Long orderId,
                                           @RequestBody RefundGuestRequestDto requestDto);
    /**
     * 비회원 반품 상세 조회
     */
    @GetMapping("/api/guest/orders/{orderId}/refunds/{refundId}")
    RefundResponseDto getRefundDetailsForGuest(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                               @PathVariable("orderId")Long orderId,
                                               @PathVariable("refundId")Long refundId);

    /**
     * 비회원 반품 신청 취소
     */
    @PostMapping("/api/guest/orders/{orderId}/refunds/{refundId}/cancel")
    RefundResponseDto cancelRefundForGuest(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable("orderId") Long orderId,
            @PathVariable("refundId") Long refundId,
            @RequestParam("guestPassword") String guestPassword
    );
}
