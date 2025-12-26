package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.WrappingPaperRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.WrappingPaperResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "gateway-service", contextId = "WrappingPaperAdminClient", url = "${gateway.base-url}")
public interface WrappingPaperAdminClient {

    /**
     * 포장지 등록
     */
    @PostMapping("/api/admin/wrappapers")
    WrappingPaperResponseDto createWrappingPaper(@RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestBody WrappingPaperRequestDto requestDto);

    /**
     * 포장지 전체 목록 조회
     */
    @GetMapping("/api/admin/wrappapers")
    Page<WrappingPaperResponseDto> getAllWrappingPapers(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                                        Pageable pageable);

    /**
     * 포장지 단건 조회
     */
    @GetMapping("/api/admin/wrappapers/{wrappingPaperId}")
    WrappingPaperResponseDto getWrappingPaper(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable("wrappingPaperId") Long wrappingPaperId
    );

    /**
     * 포장지 수정
     */
    @PutMapping("/api/admin/wrappapers/{wrappingPaperId}")
    WrappingPaperResponseDto updateWrappingPaper(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                                 @PathVariable("wrappingPaperId")Long wrappingPaperId);

    /**
     * 포장지 삭제
     */
    @DeleteMapping("/api/admin/wrappapers/{wrappingPaperId}")
    void deleteWrappingPaper(@RequestHeader(value = "Authorization", required = false) String accessToken,
                             @PathVariable("wrappingPaperId")Long wrappingPaperId);

}
