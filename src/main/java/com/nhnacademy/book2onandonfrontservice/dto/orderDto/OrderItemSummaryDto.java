package com.nhnacademy.book2onandonfrontservice.dto.orderDto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderItemSummaryDto {
    private Long bookId;      // 도서 식별자
    private String name;      // 도서 제목 (상품명)
    private Integer quantity; // 해당 도서의 주문 수량
}