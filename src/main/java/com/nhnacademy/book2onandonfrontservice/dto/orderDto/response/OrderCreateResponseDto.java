package com.nhnacademy.book2onandonfrontservice.dto.orderDto.response;

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
public class OrderCreateResponseDto {
    private Long orderId;
    private String orderNumber;
    private String orderTitle;
    private int totalItemAmount;
    private int deliveryFee;
    private int wrappingFee;
    private int couponDiscount;
    private int pointDiscount;
    private int totalDiscountAmount;
    private int totalAmount;
    private LocalDate wantDeliveryDate;

    private List<OrderItemCreateResponseDto> orderItemCreateResponseDtoList;
    private DeliveryAddressResponseDto deliveryAddressResponseDto;
}
