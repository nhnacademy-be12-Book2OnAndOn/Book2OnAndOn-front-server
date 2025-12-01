package com.nhnacademy.book2onandonfrontservice.dto.couponDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponDto {

    private Integer couponRemainingQuantity; //쿠폰 수량
    private Long couponPolicyId; //정책 id
}
