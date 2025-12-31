package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.RefundAvailableItemResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.RefundResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundRequestDto;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "gateway-service", contextId = "RefundUserClient", url = "${gateway.base-url}")
public interface RefundUserClient {

    /**
     * 회원 반품 신청 폼 : 반품 가능 품목 조회
     */
    @GetMapping("/api/orders/{orderId}/refunds/form")
    List<RefundAvailableItemResponseDto> getRefundForm(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                                       @PathVariable("orderId") Long orderId);

    /**
     * 회원 반품 신청
     */
    @PostMapping("/api/orders/{orderId}/refunds")
    RefundResponseDto createRefund(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable("orderId") Long orderId,
            @RequestBody RefundRequestDto requestDto
    );

    /**
     * 회원 반품 상세 조회
     */
    @GetMapping("/api/orders/{orderId}/refunds/{refundId}")
    RefundResponseDto getRefundDetails(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable("orderId") Long orderId,
            @PathVariable("refundId") Long refundId
    );

    /**
     * 회원 반품 신청 취소
     */
    @PostMapping("/api/orders/{orderId}/refunds/{refundId}/cancel")
    RefundResponseDto cancelRefund(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                   @PathVariable("orderId")Long orderId,
                                   @PathVariable("refundId")Long refundId);

    /**
     * 회원 전체 반품 목록 조회
     */
    @GetMapping("/api/orders/refunds/my-list")
    Page<RefundResponseDto> getMyRefunds(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                         Pageable pageable);
}
