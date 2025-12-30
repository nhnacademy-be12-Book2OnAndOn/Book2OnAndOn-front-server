package com.nhnacademy.book2onandonfrontservice.dto.orderDto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

// BookOrderResponse DTO (Book Service로부터 받을 응답)
@Getter
@NoArgsConstructor
public class BookOrderResponse {
    private Long bookId;
    private String title;
    private Long priceStandard;
    private Long priceSales;
    private String imageUrl;
    @JsonProperty("packable")
    private boolean isPackable;
    private Integer stockCount;
    private String stockStatus;
    private Long categoryId;
    private Integer quantity;
}