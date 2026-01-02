package com.nhnacademy.book2onandonfrontservice.controller.refundController;

import com.nhnacademy.book2onandonfrontservice.client.RefundUserClient;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundAvailableItemResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundRequestDto;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/refunds")
@RequiredArgsConstructor
public class RefundPageController {

    private final RefundUserClient refundUserClient;

    private String bearerOrNull(String cookieToken) {
        if (cookieToken == null || cookieToken.isBlank()) return null;
        return cookieToken.startsWith("Bearer ") ? cookieToken : "Bearer " + cookieToken;
    }

    @GetMapping("/form")
    public String refundForm(@RequestParam("orderId") Long orderId,
                             HttpServletRequest request,
                             Model model) {

        model.addAttribute("orderId", orderId);
        model.addAttribute("userType", "member");
        model.addAttribute("refundRequest", new com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundRequestDto());

        String raw = CookieUtils.getCookieValue(request, "accessToken");
        if (raw == null || raw.isBlank() || "null".equalsIgnoreCase(raw)) {
            // 토큰 없으면 인증이 필요한 기능이므로 로그인으로 보내는 게 맞음
            return "redirect:/login";
        }
        String token = raw.startsWith("Bearer ") ? raw : "Bearer " + raw;

        try {
            var items = refundUserClient.getRefundForm(token, orderId);
            model.addAttribute("items", items);
        } catch (feign.FeignException.Forbidden e) {
            // 403이면 "인증 정보가 없거나/유효하지 않음"이므로 명확히 표시
            model.addAttribute("items", List.of());
            model.addAttribute("errorMessage", "로그인이 만료되었습니다. 다시 로그인 후 시도해주세요.");
        } catch (Exception e) {
            model.addAttribute("items", List.of());
            model.addAttribute("errorMessage", "반품 가능 상품을 불러오지 못했습니다.");
        }

        return "refund/refund-form";
    }
}
