package com.nhnacademy.book2onandonfrontservice.dto.orderDto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefundItemResponseDto {
    private Long refundItemId;
    private Long orderItemId; // 원 주문 항목 ID
    private String bookTitle;
    private int refundQuantity;
}