package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.CouponPolicyDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyDiscountType;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyStatus;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;


@FeignClient(name = "gateway-service", contextId = "couponPolicyClient", url = "${gateway.base-url}")
public interface CouponPolicyClient {

    //정책 목록 조회 (페이징)
    @GetMapping("/api/admin/coupon-policies")
    Page<CouponPolicyDto> getPolicies(@RequestParam("page") int page,
                                      @RequestParam("size") int size,
                                      @RequestParam(value = "type", required = false) CouponPolicyType type,
                                      @RequestParam(value = "discountType", required = false) CouponPolicyDiscountType discountType,
                                      @RequestParam(value = "status", required = false) CouponPolicyStatus status);


    // 정책 상세 조회 (단건)
    @GetMapping("/api/admin/coupon-policies/{couponPolicyId}")
    CouponPolicyDto getPolicy(@PathVariable("couponPolicyId") Long couponPolicyId);


    //정책 생성
    @PostMapping("/api/admin/coupon-policies")
    void createPolicy(@RequestBody CouponPolicyDto requestDto);


    // 정책 수정
    @PutMapping("/api/admin/coupon-policies/{couponPolicyId}")
    void updatePolicy(@PathVariable("couponPolicyId") Long couponPolicyId,
                      @RequestBody CouponPolicyDto requestDto);

    // 정책 비활성화 (삭제)
    @DeleteMapping("/api/admin/coupon-policies/{couponPolicyId}")
    void deactivatePolicy(@PathVariable("couponPolicyId") Long couponPolicyId);

}