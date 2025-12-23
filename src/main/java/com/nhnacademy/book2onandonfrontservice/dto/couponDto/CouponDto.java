package com.nhnacademy.book2onandonfrontservice.dto.couponDto;

import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyDiscountType;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyStatus;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponDto {

    private Long couponId;
    private String couponName;
    private String discountDescription;
    private Integer discountValue;
    private CouponPolicyDiscountType discountType;
    private Integer minPrice;
    private Integer maxPrice;
    private Integer durationDays;
    private LocalDate startDate;
    private LocalDate endDate;
    private CouponPolicyStatus status;
    private Integer couponRemainingQuantity;
    private Boolean isIssued;
}
