package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.couponDto.CouponCreateDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponDto.CouponDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponDto.CouponUpdateDto;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "gateway-service", contextId = "couponClient", url = "${gateway.base-url}")
public interface CouponClient {

    @PostMapping("/api/admin/coupons")
    void createCoupon(@RequestHeader("Authorization") String accessToken, @RequestBody CouponCreateDto requestDto);

    @GetMapping("/api/admin/coupons")
    Page<CouponDto> getCoupons(@RequestHeader("Authorization") String accessToken, @RequestParam("page") int page,
                               @RequestParam("size") int size,
                               @RequestParam(value = "status", required = false) String status);

    @PutMapping("/api/admin/coupons/{coupon-id}")
    void updateCouponQuantity(@RequestHeader("Authorization") String accessToken, @PathVariable("coupon-id") Long couponId,
                              @RequestBody CouponUpdateDto updateDto);

    @GetMapping("/api/coupons/issuable")
    List<CouponDto> getAppliableCoupons(@RequestHeader("Authorization") String accessToken,
                                        @RequestParam("bookId") Long bookId,
                                        @RequestParam("categoryIds") List<Long> categoryIds);

    @PostMapping("/api/coupons/{coupon-id}")
    void issueCoupon(@RequestHeader("Authorization") String accessToken, @PathVariable("coupon-id") Long couponId);
}
