package com.nhnacademy.book2onandonfrontservice.dto.orderDto;

import java.util.List;

public class OrderRequestDto {
    private List<OrderItemRequestDto> orderItems; // 주문 상품 목록
    private String recipient;                     // 수령인
    private String recipientPhonenumber;          // 연락처
    private String deliveryAddress;               // 주소
    private String deliveryAddressDetail;         // 상세주소
    private String deliveryMessage;               // 배송 메시지
    private String wantDeliveryDate;              // 희망 배송일
    private Long couponDiscount;                  // 쿠폰 할인 금액
    private Long pointUsage;                      // 포인트 사용액
}

