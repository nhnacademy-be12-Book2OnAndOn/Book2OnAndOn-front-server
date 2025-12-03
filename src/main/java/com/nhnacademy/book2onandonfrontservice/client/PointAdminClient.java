package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.CurrentPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.EarnPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointHistoryAdminAdjustRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointHistoryResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "gateway-service", contextId = "pointAdminClient", url = "${gateway.base-url}")
public interface PointAdminClient {

    String USER_ID_HEADER = "X-USER-ID";

    // 1. 특정 유저 포인트 전체 이력 조회
    @GetMapping("/api/admin/points")
    Page<PointHistoryResponseDto> getUserPointHistory(
            @RequestParam("userId") Long userId,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );

    // 2. 특정 유저 현재 포인트 조회
    @GetMapping("/api/admin/points/current")
    CurrentPointResponseDto getUserCurrentPoint(
            @RequestParam("userId") Long userId
    );

    // 3. 관리자 수동 지급/차감
    @PostMapping("/api/admin/points/adjust")
    EarnPointResponseDto adjustPointByAdmin(
            @RequestBody PointHistoryAdminAdjustRequestDto requestDto
    );

    // 4. 포인트 만료 처리
    @PostMapping("/api/admin/points/expire")
    void expirePoints(
            @RequestHeader(USER_ID_HEADER) Long userId // admin의 userId
    );
}
