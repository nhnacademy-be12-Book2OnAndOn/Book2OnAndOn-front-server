package com.nhnacademy.book2onandonfrontservice.dto.orderDto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WrappingPaperResponseDto {
    private Long wrappingPaperId;
    private String wrappingPaperName;
    private int wrappingPaperPrice;
    private String wrappingPaperPath;
}