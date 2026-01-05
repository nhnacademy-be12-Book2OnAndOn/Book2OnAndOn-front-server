package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundAvailableItemResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundGuestRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundResponseDto;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "gateway-service", contextId = "RefundGuestClient", url = "${gateway.base-url}")
public interface RefundGuestClient {

    @GetMapping("/api/guest/orders/{orderId}/refunds/form")
    List<RefundAvailableItemResponseDto> getRefundFormForGuest(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("orderId") Long orderId
    );

    @PostMapping("/api/guest/orders/{orderId}/refunds")
    RefundResponseDto createRefundForGuest(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId,
            @RequestBody RefundGuestRequestDto requestDto
    );

    @GetMapping("/api/guest/orders/{orderId}/refunds/{refundId}")
    RefundResponseDto getRefundDetailsForGuest(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("orderId") Long orderId,
            @PathVariable("refundId") Long refundId
    );

    // 서버는 /cancel suffix 존재
    @PostMapping("/api/guest/orders/{orderId}/refunds/{refundId}/cancel")
    RefundResponseDto cancelRefundForGuest(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("orderId") Long orderId,
            @PathVariable("refundId") Long refundId,
            @RequestParam("guestPassword") String guestPassword
    );
}
