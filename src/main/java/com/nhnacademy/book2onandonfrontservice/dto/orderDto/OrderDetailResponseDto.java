package com.nhnacademy.book2onandonfrontservice.dto.orderDto;

import java.util.List;

public class OrderDetailResponseDto {
    private String orderNumber;             // 주문 번호
    private String orderDate;               // 주문 일시
    private String status;                  // 현재 상태
    private String recipient;               // 수령인
    private String address;                 // 기본 주소
    private String addressDetail;           // 상세 주소
    private Long totalAmount;               // 최종 결제 금액
    private List<OrderDetailItemDto> items; // 상세 상품 목록 (포장 정보 포함)
}