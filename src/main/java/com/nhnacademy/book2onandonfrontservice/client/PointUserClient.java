package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.CurrentPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.EarnOrderPointRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.EarnPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.EarnReviewPointRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointHistoryResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.RefundPointRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.UsePointRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "gateway-service", contextId = "pointUserClient", url = "${gateway.base-url}")
public interface PointUserClient {

    // 1. 내 포인트 이력 조회
    @GetMapping("/api/users/me/points")
    Page<PointHistoryResponseDto> getMyPointHistory(
            @RequestHeader("Authorization") String accessToken,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );

    // 2. 내 현재 포인트 조회
    @GetMapping("/api/users/me/points/current")
    CurrentPointResponseDto getMyCurrentPoint(
            @RequestHeader("Authorization") String accessToken
    );

    // 3-1. 회원가입 적립
    @PostMapping("/api/users/me/points/earn/signup")
    EarnPointResponseDto earnSignupPoint(
            @RequestHeader("Authorization") String accessToken
    );

    // 3-2. 리뷰 작성 적립
    @PostMapping("/api/users/me/points/earn/review")
    EarnPointResponseDto earnReviewPoint(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody EarnReviewPointRequestDto dto
    );

    // 3-3. 주문 적립
    @PostMapping("/api/users/me/points/earn/order")
    EarnPointResponseDto earnOrderPoint(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody EarnOrderPointRequestDto dto
    );

    // 4. 포인트 사용
    @PostMapping("/api/users/me/points/use")
    EarnPointResponseDto usePoint(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody UsePointRequestDto dto
    );

    // 5. 포인트 반환(반품/취소)
    @PostMapping("/api/users/me/points/refund")
    EarnPointResponseDto refundPoint(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody RefundPointRequestDto dto
    );
}
