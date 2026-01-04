package com.nhnacademy.book2onandonfrontservice.dto.orderDto.response;

import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyDiscountType;
import java.util.List;

public record CouponTargetResponseDto(Long memberCouponId,
                                      List<Long> targetBookIds,
                                      List<Long> targetCategoryIds,
                                      Integer minPrice,
                                      Integer maxPrice,
                                      CouponPolicyDiscountType discountType,
                                      Integer discountValue) {
}
