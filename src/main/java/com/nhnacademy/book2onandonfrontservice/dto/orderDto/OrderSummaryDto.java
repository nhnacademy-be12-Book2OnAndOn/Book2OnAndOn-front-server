package com.nhnacademy.book2onandonfrontservice.dto.orderDto;

import java.util.List;

public class OrderSummaryDto {
    private String orderId;                 // 주문 번호
    private String date;                    // 주문 일자
    private Long total;                     // 총 결제 금액
    private String status;                  // 주문 상태 (ENUM과 매핑)
    private List<OrderItemSummaryDto> items; // 대표 상품 정보
}