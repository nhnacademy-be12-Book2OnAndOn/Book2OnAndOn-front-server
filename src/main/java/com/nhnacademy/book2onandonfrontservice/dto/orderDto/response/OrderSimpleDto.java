package com.nhnacademy.book2onandonfrontservice.dto.orderDto.response;

import com.nhnacademy.book2onandonfrontservice.dto.orderDto.status.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 주문 목록 페이지에서 핵심 요약 정보만 제공
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderSimpleDto {
    private Long orderId;
    private String orderNumber;
    private OrderStatus orderStatus;
    private LocalDateTime orderDateTime;
    private int totalAmount; // 최종 결제 금액
    /** 대표 상품명  */
    private String orderTitle;
}