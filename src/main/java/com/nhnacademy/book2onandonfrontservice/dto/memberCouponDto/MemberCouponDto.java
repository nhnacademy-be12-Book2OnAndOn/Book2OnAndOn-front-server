package com.nhnacademy.book2onandonfrontservice.dto.memberCouponDto;

import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyDiscountType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberCouponDto {
    private Long memberCouponId;
    private String couponName;                // CouponPolicy
    private Integer minPrice;                 // CouponPolicy
    private Integer maxPrice;                 // CouponPolicy
    private Integer discountValue;            // CouponPolicy
    private CouponPolicyDiscountType discountType;
    private MemberCouponStatus memberCouponStatus;   // MemberCoupon
    private LocalDateTime memberCouponEndDate;       // MemberCoupon
    private LocalDateTime memberCouponUseDate;       // MemberCoupon
    private String discountDescription;
}
