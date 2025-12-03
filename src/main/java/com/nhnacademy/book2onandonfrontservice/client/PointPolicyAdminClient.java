package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointPolicy.PointPolicyActiveUpdateRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointPolicy.PointPolicyResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointPolicy.PointPolicyUpdateRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "gateway-service", contextId = "pointPolicyAdminClient", url = "${gateway.base-url}")
public interface PointPolicyAdminClient {

    String USER_ID_HEADER = "X-USER-ID";

    // 1. 정책 전체 조회
    @GetMapping("/api/admin/point-policies")
    List<PointPolicyResponseDto> getAllPolicies(
            @RequestHeader(USER_ID_HEADER) Long userId // admin's userId
    );

    // 2. 정책 단건 조회
    @GetMapping("/api/admin/point-policies/{policyName}")
    PointPolicyResponseDto getPolicy(
            @PathVariable("policyName") String policyName,
            @RequestHeader(USER_ID_HEADER) Long userId // admin's userId
    );

    // 3. 정책 수정
    @PutMapping("/api/admin/point-policies/{policyId}")
    PointPolicyResponseDto updatePolicy(
            @PathVariable("policyId") Integer policyId,
            @RequestBody PointPolicyUpdateRequestDto dto,
            @RequestHeader(USER_ID_HEADER) Long userId // admin's userId
    );

    // 4. 정책 활성/비활성
    @PatchMapping("/api/admin/point-policies/{policyId}/active")
    PointPolicyResponseDto updatePolicyActive(
            @PathVariable("policyId") Integer policyId,
            @RequestBody PointPolicyActiveUpdateRequestDto dto,
            @RequestHeader(USER_ID_HEADER) Long userId // admin's userId
    );
}
