package com.nhnacademy.book2onandonfrontservice.dto.orderDto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 개별 상품 주문 정보를 담는 DTO. OrderCreateRequestDto와 GuestOrderCreateDto에 사용
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequestDto {
    private Long bookId;
    private int quantity;   // or
    private boolean isWrapped;// der_item_quantity
    private Long wrappingPaperId;
}