package com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto;

import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyDiscountType;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyStatus;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyType;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CouponPolicyDto {

    private Long couponPolicyId; // 정책 id
    private String couponPolicyName;  // 정책 이름
    private CouponPolicyType couponPolicyType; // 정책 타입
    private CouponPolicyDiscountType couponPolicyDiscountType; // 할인 타입
    private Integer couponDiscountValue; // 할인율 / 금액
    private Integer minPrice; // 최조주문금액
    private Integer maxPrice; // 최대할인금액

    private Integer durationDays; // 상대기간
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fixedStartDate; // 고정기간 시작
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fixedEndDate; // 고정기간 끝

    private List<Long> targetBookIds; // bookId
    private List<Long> targetCategoryIds; //categoryId
    private CouponPolicyStatus couponPolicyStatus; // 정책 활성화 상태

}
