package com.nhnacademy.book2onandonfrontservice.dto.orderDto.request;

import com.nhnacademy.book2onandonfrontservice.dto.orderDto.status.OrderItemStatus;

public record OrderItemStatusUpdateDto(Long orderItemId, OrderItemStatus orderItemStatus) {
}
