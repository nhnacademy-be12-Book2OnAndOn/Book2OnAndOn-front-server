package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundSearchCondition;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundStatusUpdateRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "gateway-service", contextId = "RefundAdminClient", url = "${gateway.base-url}")
public interface RefundAdminClient {

    /**
     * 관리자 반품 목록 조회(검색)
     * @SpringQueryMap: 객체의 필드를 쿼리 파라미터로 펼쳐서 보냄
     */
    @GetMapping("/api/admin/refunds")
    Page<RefundResponseDto> getRefundList(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                          @SpringQueryMap RefundSearchCondition condition,
                                          Pageable pageable);

    /**
     * 관리자 반품 상세조회
     * GET /api/admin/refunds/{refundId}
     */
    @GetMapping("/api/admin/refunds/{refundId}")
    RefundResponseDto findRefundDetails(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                        @PathVariable Long refundId);

    /**
     * 관리자 반품 상태변경
     * PatchMapping /api/admin/refunds/{refundId}
     */
    @PatchMapping("/api/admin/refunds/{refundId}")
    RefundResponseDto updateRefundStatus(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                         @PathVariable Long refundId,
                                         @RequestBody RefundStatusUpdateRequestDto requestDto);
}
