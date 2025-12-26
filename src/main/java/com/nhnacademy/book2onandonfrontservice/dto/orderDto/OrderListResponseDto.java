package com.nhnacademy.book2onandonfrontservice.dto.orderDto;

import java.util.List;

public class OrderListResponseDto {
    private String name;                // 화면 상단에 표시할 사용자 닉네임
    private List<OrderSummaryDto> orders;   // 주문 요약 목록
}
