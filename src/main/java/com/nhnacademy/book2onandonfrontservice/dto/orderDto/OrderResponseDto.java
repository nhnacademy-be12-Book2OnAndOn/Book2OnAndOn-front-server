package com.nhnacademy.book2onandonfrontservice.dto.orderDto;

public class OrderResponseDto {
    private String orderNumber; // 실제 DB에서 생성된 주문번호
    private Long totalAmount;   // 서버에서 최종 검증된 결제 금액
}