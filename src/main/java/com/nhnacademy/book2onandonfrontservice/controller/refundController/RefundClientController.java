package com.nhnacademy.book2onandonfrontservice.controller.refundController;

import com.nhnacademy.book2onandonfrontservice.client.RefundAdminClient;
import com.nhnacademy.book2onandonfrontservice.client.RefundGuestClient;
import com.nhnacademy.book2onandonfrontservice.client.RefundUserClient;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundAvailableItemResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundGuestRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundSearchCondition;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundStatusUpdateRequestDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/refunds")
@RequiredArgsConstructor
public class RefundClientController {

    private final RefundUserClient refundUserClient;
    private final RefundGuestClient refundGuestClient;
    private final RefundAdminClient refundAdminClient;

    private String bearerOrNull(String cookieToken) {
        if (cookieToken == null || cookieToken.isBlank()) return null;
        return cookieToken.startsWith("Bearer ") ? cookieToken : "Bearer " + cookieToken;
    }

    // =========================
    // 회원 반품
    // =========================

    @PostMapping("/orders/{orderId}")
    public RefundResponseDto createRefundForMember(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @PathVariable Long orderId,
            @Valid @RequestBody RefundRequestDto request
    ) {
        return refundUserClient.createRefund(bearerOrNull(accessToken), orderId, request);
    }

    @PostMapping("/orders/{orderId}/{refundId}/cancel")
    public RefundResponseDto cancelRefundForMember(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @PathVariable Long orderId,
            @PathVariable Long refundId
    ) {
        return refundUserClient.cancelRefund(bearerOrNull(accessToken), orderId, refundId);
    }

    @GetMapping("/orders/{orderId}/{refundId}")
    public RefundResponseDto getRefundDetailsForMember(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @PathVariable Long orderId,
            @PathVariable Long refundId
    ) {
        return refundUserClient.getRefundDetails(bearerOrNull(accessToken), orderId, refundId);
    }

    // 주의: 너희 Feign은 "내 목록"이며 orderId를 받지 않음
    @GetMapping("/my-list")
    public Page<RefundResponseDto> getMyRefunds(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            Pageable pageable
    ) {
        return refundUserClient.getMyRefunds(bearerOrNull(accessToken), pageable);
    }

    @GetMapping("/orders/{orderId}/form")
    public List<RefundAvailableItemResponseDto> getRefundFormForMember(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @PathVariable Long orderId
    ) {
        return refundUserClient.getRefundForm(bearerOrNull(accessToken), orderId);
    }

    // =========================
    // 비회원 반품
    // =========================
    // Feign이 Authorization 헤더를 받으므로 guestOrderToken을 Authorization로 전달한다고 가정
    // (게이트웨이 정책이 다르면 이 부분이 401/403 원인이 됨)

    @PostMapping("/guest/orders/{orderId}")
    public RefundResponseDto createRefundForGuest(
            @CookieValue(value = "guestOrderToken", required = false) String guestOrderToken,
            @PathVariable Long orderId,
            @Valid @RequestBody RefundGuestRequestDto request
    ) {
        return refundGuestClient.createRefundForGuest(guestOrderToken, orderId, request);
    }

    @PostMapping("/guest/orders/{orderId}/{refundId}/cancel")
    public RefundResponseDto cancelRefundForGuest(
            @CookieValue(value = "guestOrderToken", required = false) String guestOrderToken,
            @PathVariable Long orderId,
            @PathVariable Long refundId,
            @RequestParam String guestPassword
    ) {
        return refundGuestClient.cancelRefundForGuest(guestOrderToken, orderId, refundId, guestPassword);
    }

    @GetMapping("/guest/orders/{orderId}/{refundId}")
    public RefundResponseDto getRefundDetailsForGuest(
            @CookieValue(value = "guestOrderToken", required = false) String guestOrderToken,
            @PathVariable Long orderId,
            @PathVariable Long refundId
    ) {
        return refundGuestClient.getRefundDetailsForGuest(guestOrderToken, orderId, refundId);
    }

    @GetMapping("/guest/orders/{orderId}/form")
    public List<RefundAvailableItemResponseDto> getRefundFormForGuest(
            @CookieValue(value = "guestOrderToken", required = false) String guestOrderToken,
            @PathVariable Long orderId
    ) {
        return refundGuestClient.getRefundFormForGuest(guestOrderToken, orderId);
    }

    // =========================
    // 관리자 반품
    // =========================

    @GetMapping("/admin")
    public Page<RefundResponseDto> getRefundListForAdmin(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @ModelAttribute RefundSearchCondition condition,
            Pageable pageable
    ) {
        return refundAdminClient.getRefundList(bearerOrNull(accessToken), condition, pageable);
    }

    @GetMapping("/admin/{refundId}")
    public RefundResponseDto getRefundDetailsForAdmin(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @PathVariable Long refundId
    ) {
        return refundAdminClient.findRefundDetails(bearerOrNull(accessToken), refundId);
    }

    @PatchMapping("/admin/{refundId}")
    public RefundResponseDto updateRefundStatusForAdmin(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @PathVariable Long refundId,
            @Valid @RequestBody RefundStatusUpdateRequestDto request
    ) {
        return refundAdminClient.updateRefundStatus(bearerOrNull(accessToken), refundId, request);
    }
}
