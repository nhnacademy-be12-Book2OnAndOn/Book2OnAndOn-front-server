package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.CouponPolicyDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.CouponPolicyUpdateDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyDiscountType;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyStatus;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "gateway-service", contextId = "couponPolicyClient", url = "${gateway.base-url}")
public interface CouponPolicyClient {

    // 정책 목록 조회 (페이징)
    @GetMapping("/api/admin/coupon-policies")
    Page<CouponPolicyDto> getPolicies(@RequestHeader("Authorization") String accessToken,
                                      @RequestParam("page") int page,
                                      @RequestParam("size") int size,
                                      @RequestParam(value = "type", required = false) CouponPolicyType type,
                                      @RequestParam(value = "discountType", required = false) CouponPolicyDiscountType discountType,
                                      @RequestParam(value = "status", required = false) CouponPolicyStatus status);


    // 정책 상세 조회 (단건)
    @GetMapping("/api/admin/coupon-policies/{coupon-policy-id}")
    CouponPolicyDto getPolicy(@RequestHeader("Authorization") String accessToken,
                              @PathVariable("coupon-policy-id") Long couponPolicyId);


    // 정책 생성
    @PostMapping("/api/admin/coupon-policies")
    void createPolicy(@RequestHeader("Authorization") String accessToken,
                      @RequestBody CouponPolicyUpdateDto requestDto);


    // 정책 수정
    @PutMapping("/api/admin/coupon-policies/{coupon-policy-id}")
    void updatePolicy(@RequestHeader("Authorization") String accessToken,
                      @PathVariable("coupon-policy-id") Long couponPolicyId,
                      @RequestBody CouponPolicyUpdateDto requestDto);

    // 정책 비활성화 (삭제)
    @DeleteMapping("/api/admin/coupon-policies/{coupon-policy-id}")
    void deactivatePolicy(@RequestHeader("Authorization") String accessToken,
                          @PathVariable("coupon-policy-id") Long couponPolicyId);

}