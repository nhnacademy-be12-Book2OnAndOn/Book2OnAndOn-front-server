package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.WrappingPaperSimpleResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "gateway-service", contextId = "WrappingPaperClient", url = "${gateway.base-url}")
public interface WrappingPaperClient {
    /**
     * 사용자 포장지 목록 조회
     * 주문서 작성 화면에서 사용됨
     */
    @GetMapping("/api/wrappapers")
    Page<WrappingPaperSimpleResponseDto> getWrappingPaperList(
            @RequestHeader(value = "Authorization", required = false)String accessToken,
            Pageable pageable
    );
}
