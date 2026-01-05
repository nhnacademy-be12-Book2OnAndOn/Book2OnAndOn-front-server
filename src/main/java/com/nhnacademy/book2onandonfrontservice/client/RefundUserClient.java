package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundAvailableItemResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundResponseDto;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "gateway-service", contextId = "RefundUserClient", url = "${gateway.base-url}")
public interface RefundUserClient {

    @GetMapping("/api/orders/{orderId}/refunds/form")
    List<RefundAvailableItemResponseDto> getRefundForm(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("orderId") Long orderId
    );

    @PostMapping("/api/orders/{orderId}/refunds")
    RefundResponseDto createRefund(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("orderId") Long orderId,
            @RequestBody RefundRequestDto requestDto
    );

    // 서버는 /orders/{orderId}/refund/{refundId} (refund 단수)
    @GetMapping("/api/orders/{orderId}/refunds/{refundId}")
    RefundResponseDto getRefundDetails(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("orderId") Long orderId,
            @PathVariable("refundId") Long refundId
    );

    @PostMapping("/api/orders/{orderId}/refunds/{refundId}/cancel")
    RefundResponseDto cancelRefund(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("orderId") Long orderId,
            @PathVariable("refundId") Long refundId
    );

    @GetMapping("/api/orders/refunds/my-list")
    Page<RefundResponseDto> getMyRefunds(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            Pageable pageable
    );
}
