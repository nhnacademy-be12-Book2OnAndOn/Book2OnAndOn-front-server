package com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto;

import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyDiscountType;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyStatus;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyType;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CouponPolicyUpdateDto {

    private String couponPolicyName;
    private CouponPolicyType couponPolicyType;
    private CouponPolicyDiscountType couponPolicyDiscountType;
    private Integer couponDiscountValue;
    private Integer minPrice;

    private Integer maxPrice;
    private Boolean removeMaxPrice;

    private Integer durationDays;
    private Boolean removeDurationDays;

    private LocalDate fixedStartDate;
    private LocalDate fixedEndDate;
    private Boolean removeFixedDate;


    private List<Long> targetBookIds;
    private Boolean removeTargetBook;

    private List<Long> targetCategoryIds;
    private Boolean removeTargetCategory;

    private CouponPolicyStatus couponPolicyStatus;
}
