package com.nhnacademy.book2onandonfrontservice.dto.orderDto.response;


import com.nhnacademy.book2onandonfrontservice.dto.orderDto.status.OrderItemStatus;

public record OrderItemCreateResponseDto(Long orderItemId,
                                         Long bookId,
                                         Integer orderItemQuantity,
                                         Integer unitPrice,
                                         boolean isWrapped,
                                         OrderItemStatus orderItemStatus,
                                         Long wrappingPaperId) {
}
