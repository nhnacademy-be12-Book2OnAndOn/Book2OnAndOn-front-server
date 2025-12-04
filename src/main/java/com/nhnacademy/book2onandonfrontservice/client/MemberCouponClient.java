package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.memberCouponDto.MemberCouponDto;
import com.nhnacademy.book2onandonfrontservice.dto.memberCouponDto.MemberCouponStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "gateway-service", contextId = "memberCouponClient", url = "${gateway.base-url}")
public interface MemberCouponClient {

    @GetMapping("/api/my-coupon")
    Page<MemberCouponDto> getMyCoupon(@RequestHeader("Authorization") String accessToken,
                                      @RequestParam("page") int page,
                                      @RequestParam("size") int size,
                                      @RequestParam(value = "status", required = false) MemberCouponStatus status);
}
