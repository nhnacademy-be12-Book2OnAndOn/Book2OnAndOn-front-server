package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.couponDto.CouponDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "gateway-service", contextId = "couponClient", url = "${gateway.base-url}")
public interface CouponClient {

    @PostMapping("api/admin/coupons")
    void createCoupon(@RequestBody CouponDto requestDto);

}
