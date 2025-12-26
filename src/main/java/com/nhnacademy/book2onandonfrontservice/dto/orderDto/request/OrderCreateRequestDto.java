package com.nhnacademy.book2onandonfrontservice.dto.orderDto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequestDto {
    private List<OrderItemRequestDto> orderItems;
    private DeliveryAddressRequestDto deliveryAddress;
    @NotNull(message = "배송방법을 선택해주세요")
    private Long deliveryPolicyId; // 배송 방법 아이디
    @NotNull(message = "원하는 배송날짜를 선택해주세요")
    private LocalDate wantDeliveryDate;
    private Long memberCouponId; // 하나의 주문에 하나의 쿠폰만 사용
    private Integer point;
}