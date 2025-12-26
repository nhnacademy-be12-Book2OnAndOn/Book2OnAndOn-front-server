package com.nhnacademy.book2onandonfrontservice.dto.orderDto;

public class OrderItemRequestDto {
    private Long bookId;           // 도서 ID
    private Integer quantity;      // 수량
    private Long wrappingPaperId;  // 포장지 ID (없으면 null)
    private Boolean isWrapped;     // 포장 여부
}
