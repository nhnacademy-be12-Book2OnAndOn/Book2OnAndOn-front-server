package com.nhnacademy.book2onandonfrontservice.controller.couponController;

import com.nhnacademy.book2onandonfrontservice.dto.couponDto.CouponDto; // 패키지명 확인 필요
import com.nhnacademy.book2onandonfrontservice.client.CouponClient; // FeignClient import
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest; // Spring Boot 3.x 기준
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponClient couponClient;

    @GetMapping("/appliable")
    public ResponseEntity<List<CouponDto>> getAppliableCoupons(
            @RequestParam("bookId") Long bookId,
            @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds) {

        if (categoryIds == null) {
            categoryIds = List.of();
        }

        try {
            List<CouponDto> coupons = couponClient.getAppliableCoupons(bookId, categoryIds);
            return ResponseEntity.ok(coupons);
        } catch (Exception e) {
            log.error("쿠폰 조회 실패", e);
            throw e; // 프론트의 GlobalExceptionHandler가 처리하거나 500 에러 반환
        }
    }

    /**
     * [기능 2] 쿠폰 발급 요청
     * - CookieUtils를 사용하여 토큰 추출 후 백엔드 호출
     */
    @PostMapping("/{couponId}/issue")
    public ResponseEntity<String> issueCoupon(
            @PathVariable("couponId") Long couponId,
            HttpServletRequest request) {

        // 1. 쿠키에서 accessToken 값 꺼내기
        String cookieValue = CookieUtils.getCookieValue(request, "accessToken");

        // 2. 토큰이 없으면 401 에러 반환 (JS에서 로그인 페이지로 이동시킴)
        if (cookieValue == null || cookieValue.isEmpty()) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        // 3. Bearer 붙이기
        String token = "Bearer " + cookieValue;

        try {
            // 4. FeignClient 호출 (헤더에 토큰 전달)
            couponClient.issueCoupon(token, couponId);
            return ResponseEntity.ok("쿠폰이 발급되었습니다.");

        } catch (FeignException e) {
            log.error("쿠폰 발급 실패 (Feign): status={}, message={}", e.status(), e.getMessage());

            // 백엔드에서 보낸 에러 메시지(예: "이미 발급된 쿠폰입니다")를 그대로 전달
            // e.contentUTF8()을 쓰면 백엔드의 Body 메시지를 읽을 수 있습니다.
            String serverMessage = e.contentUTF8();
            if (serverMessage == null || serverMessage.isEmpty()) {
                serverMessage = "발급 실패 (오류 코드: " + e.status() + ")";
            }

            return ResponseEntity.status(e.status()).body(serverMessage);

        } catch (Exception e) {
            log.error("쿠폰 발급 실패 (System)", e);
            return ResponseEntity.internalServerError().body("시스템 오류가 발생했습니다.");
        }
    }
}