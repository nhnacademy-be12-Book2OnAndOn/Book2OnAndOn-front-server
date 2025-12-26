package com.nhnacademy.book2onandonfrontservice.dto.paymentDto.request;

public record CommonConfirmRequest(String orderId,
                                   String paymentKey,
                                   Integer amount) {

    // 토스 응답 변환
    public TossConfirmRequest toTossConfirmRequest(){
        return new TossConfirmRequest(
                this.orderId,
                this.paymentKey,
                this.amount
        );
    }
}