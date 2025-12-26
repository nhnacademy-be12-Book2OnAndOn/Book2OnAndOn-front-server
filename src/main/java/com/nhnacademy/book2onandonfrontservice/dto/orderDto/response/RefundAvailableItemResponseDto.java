package com.nhnacademy.book2onandonfrontservice.dto.orderDto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefundAvailableItemResponseDto {
    private Long orderItemId;
    private Long bookId;
    private String bookTitle;
    private int orderedQuantity;
    private int alreadyReturnedQuantity;
    private int returnableQuantity; // ordered - alreadyReturned
    private boolean activeRefundExists;
    private boolean refundable; // 정책/상태상 가능한지(예: 수량 0이면 false)
}