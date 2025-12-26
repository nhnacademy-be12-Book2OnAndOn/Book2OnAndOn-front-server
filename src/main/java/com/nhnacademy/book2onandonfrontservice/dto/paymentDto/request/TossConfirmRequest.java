package com.nhnacademy.book2onandonfrontservice.dto.paymentDto.request;

public record TossConfirmRequest(String orderId,
                                 String paymentKey,
                                 Integer amount) {
}