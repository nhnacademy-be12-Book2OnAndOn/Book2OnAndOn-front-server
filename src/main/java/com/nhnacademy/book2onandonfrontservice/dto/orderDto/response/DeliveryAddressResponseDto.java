package com.nhnacademy.book2onandonfrontservice.dto.orderDto.response;

public record DeliveryAddressResponseDto(String deliveryAddress,
                                         String deliveryAddressDetail,
                                         String deliveryMessage,
                                         String recipient,
                                         String recipientPhoneNumber) {
}
