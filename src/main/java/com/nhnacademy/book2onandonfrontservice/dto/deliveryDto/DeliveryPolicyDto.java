package com.nhnacademy.book2onandonfrontservice.dto.deliveryDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPolicyDto {
    private Long deliveryPolicyId;
    private String deliveryPolicyName; // 배송 정책명
    private Integer deliveryFee; // 배송비
    private Integer freeDeliveryThreshold; //무료 배송 기준 금액
}
