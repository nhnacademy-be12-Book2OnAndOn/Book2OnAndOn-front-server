package com.nhnacademy.book2onandonfrontservice.dto.orderDto.response;


import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.CurrentPointResponseDto;
import java.util.List;

public record
OrderPrepareResponseDto(
        // 책 아이템 조회
        List<BookOrderResponse> orderItems,
        // 유저 배송지 조회
        List<UserAddressResponseDto> addresses,
        // 사용할 수 있는 쿠폰 조회
        List<MemberCouponResponseDto> coupons,

        List<CouponTargetResponseDto> couponTargets,
        // 유저 포인트 조회
        CurrentPointResponseDto currentPoint
) {
}
