package com.nhnacademy.book2onandonfrontservice.controller.refundController;

import com.nhnacademy.book2onandonfrontservice.client.RefundGuestClient;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundAvailableItemResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundGuestRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundResponseDto;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/guest/refunds")
public class RefundGuestController {

    private final RefundGuestClient refundGuestClient;

    // 비회원: 반품 신청 폼
    // GET /guest/refunds/orders/{orderId}/form
    @GetMapping("/orders/{orderId}/form")
    public String guestRefundForm(@PathVariable Long orderId,
                                  HttpServletRequest request,
                                  Model model,
                                  RedirectAttributes ra) {
        String accessToken = CookieUtils.getCookieValue(request, "accessToken");
        if (accessToken == null || accessToken.isBlank() || "null".equals(accessToken)) {
            return "redirect:/login";
        }
        String token = "Bearer " + accessToken;

        try {
            List<RefundAvailableItemResponseDto> items = refundGuestClient.getRefundFormForGuest(token, orderId);
            model.addAttribute("userType", "guest");
            model.addAttribute("orderId", orderId);
            model.addAttribute("items", items);
            model.addAttribute("refundRequest", new RefundGuestRequestDto());
            return "refund/refund-form";
        } catch (Exception e) {
            log.error("비회원 반품 폼 조회 실패. orderId={}", orderId, e);
            ra.addFlashAttribute("error", "비회원 반품 신청 폼을 불러오지 못했습니다.");
            return "redirect:/guest/orders/" + orderId;
        }
    }

    // 비회원: 반품 신청 제출
    // POST /guest/refunds/orders/{orderId}
    @PostMapping("/orders/{orderId}")
    public String submitGuestRefund(@PathVariable Long orderId,
                                    HttpServletRequest request,
                                    @ModelAttribute RefundGuestRequestDto refundRequestDto,
                                    RedirectAttributes ra) {
        String token = CookieUtils.getCookieValue(request, "accessToken");
        if (token != null && !token.startsWith("Bearer ")) token = "Bearer " + token;

        try {
            RefundResponseDto created = refundGuestClient.createRefundForGuest(token, orderId, refundRequestDto);
            ra.addFlashAttribute("message", "비회원 반품 신청이 접수되었습니다.");
            return "redirect:/guest/refunds/orders/" + orderId + "/" + created.getRefundId();
        } catch (Exception e) {
            log.error("비회원 반품 신청 실패. orderId={}", orderId, e);
            ra.addFlashAttribute("error", "비회원 반품 신청에 실패했습니다.");
            return "redirect:/guest/refunds/orders/" + orderId + "/form";
        }
    }

    // 비회원: 반품 상세
    // GET /guest/refunds/orders/{orderId}/{refundId}
    @GetMapping("/orders/{orderId}/{refundId}")
    public String guestRefundDetail(@PathVariable Long orderId,
                                    @PathVariable Long refundId,
                                    HttpServletRequest request,
                                    Model model,
                                    RedirectAttributes ra) {
        String token = CookieUtils.getCookieValue(request, "accessToken");
        if (token != null && !token.startsWith("Bearer ")) token = "Bearer " + token;

        try {
            RefundResponseDto detail = refundGuestClient.getRefundDetailsForGuest(token, orderId, refundId);
            model.addAttribute("detail", detail);
            return "refund/guest/detail";
        } catch (Exception e) {
            log.error("비회원 반품 상세 조회 실패. orderId={}, refundId={}", orderId, refundId, e);
            ra.addFlashAttribute("error", "비회원 반품 상세를 불러오지 못했습니다.");
            return "redirect:/guest/orders/" + orderId;
        }
    }

    // 비회원: 반품 신청 취소
    // POST /guest/refunds/orders/{orderId}/{refundId}/cancel
    @PostMapping("/orders/{orderId}/{refundId}/cancel")
    public String cancelGuestRefund(@PathVariable Long orderId,
                                    @PathVariable Long refundId,
                                    @RequestParam String guestPassword,
                                    HttpServletRequest request,
                                    RedirectAttributes ra) {
        String token = CookieUtils.getCookieValue(request, "accessToken");
        if (token != null && !token.startsWith("Bearer ")) token = "Bearer " + token;

        try {
            refundGuestClient.cancelRefundForGuest(token, orderId, refundId, guestPassword);
            ra.addFlashAttribute("message", "비회원 반품 신청이 취소되었습니다.");
        } catch (Exception e) {
            log.error("비회원 반품 취소 실패. orderId={}, refundId={}", orderId, refundId, e);
            ra.addFlashAttribute("error", "비회원 반품 취소에 실패했습니다.");
        }
        return "redirect:/guest/refunds/orders/" + orderId + "/" + refundId;
    }
}
