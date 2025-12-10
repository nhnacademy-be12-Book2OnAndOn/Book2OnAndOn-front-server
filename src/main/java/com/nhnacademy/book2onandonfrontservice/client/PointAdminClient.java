package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.CurrentPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.EarnPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointHistoryAdminAdjustRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointHistoryResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "gateway-service", contextId = "pointAdminClient", url = "${gateway.base-url}")
public interface PointAdminClient {


    // 1. 특정 유저 포인트 전체 이력 조회
    @GetMapping("/api/admin/points")
    Page<PointHistoryResponseDto> getUserPointHistory(
            @RequestHeader("Authorization") String accessToken,
            @RequestParam("userId") Long userId,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );

    // 2. 특정 유저 현재 포인트 조회
    @GetMapping("/api/admin/points/current")
    CurrentPointResponseDto getUserCurrentPoint(
            @RequestHeader("Authorization") String accessToken,
            @RequestParam("userId") Long userId
    );

    // 3. 관리자 수동 지급/차감
    @PostMapping("/api/admin/points/adjust")
    EarnPointResponseDto adjustPointByAdmin(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody PointHistoryAdminAdjustRequestDto requestDto
    );
}
