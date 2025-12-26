package com.nhnacademy.book2onandonfrontservice.dto.orderDto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 포장지 목록 조회 시 사용(사용자 확인용)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WrappingPaperSimpleResponseDto {
    private Long wrappingPaperId;
    private String wrappingPaperName;
    private int wrappingPaperPrice;
}