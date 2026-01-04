package com.nhnacademy.book2onandonfrontservice.dto.orderDto.request;

import com.nhnacademy.book2onandonfrontservice.dto.orderDto.status.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 *  주문 상태 변경 요청 시 사용
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateDto {
    private OrderStatus orderStatus;
}