package com.nhnacademy.book2onandonfrontservice.controller.refundController;

import com.nhnacademy.book2onandonfrontservice.client.RefundUserClient;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.RefundAvailableItemResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.RefundResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundRequestDto;
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
@RequestMapping("/guest/refunds") // 프론트 URL prefix (예시)
public class RefundGuestController {

    private final RefundUserClient refundUserClient;

    // 비회원: 반품 신청 폼
    // GET /guest/refunds/orders/{orderId}/form
    @GetMapping("/orders/{orderId}/form")
    public String guestRefundForm(@PathVariable Long orderId,
                                  @CookieValue(name = "guestOrderToken", required = false) String guestToken,
                                  Model model,
                                  RedirectAttributes ra) {

        if (guestToken == null) {
            return "redirect:/guest/login";
        }


        try {
            List<RefundAvailableItemResponseDto> items = refundUserClient.getRefundForm(null, guestToken, orderId);
            model.addAttribute("orderId", orderId);
            model.addAttribute("items", items);
            model.addAttribute("refundRequest", new RefundRequestDto());
            return "refund/refund-form";
        } catch (Exception e) {
            log.error("비회원 반품 폼 조회 실패. orderId={}", orderId, e);
            ra.addFlashAttribute("error", "비회원 반품 신청 폼을 불러오지 못했습니다.");
            return "redirect:/guest/orders/" + orderId; // 비회원 주문조회 상세 페이지로 조정
        }
    }

    // 비회원: 반품 신청 제출
    // POST /guest/refunds/orders/{orderId}
    @PostMapping("/orders/{orderId}")
    public String submitGuestRefund(@PathVariable Long orderId,
                                    @CookieValue(name = "guestOrderToken", required = false) String guestToken,
                                    @ModelAttribute RefundRequestDto refundRequestDto,
                                    RedirectAttributes ra) {

        if (guestToken == null) {
            return "redirect:/guest/login";
        }

        try {
            RefundResponseDto created = refundUserClient.createRefund(null, guestToken, orderId, refundRequestDto);
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
                                    @CookieValue(name = "guestOrderToken", required = false) String guestToken,
                                    Model model,
                                    RedirectAttributes ra) {

        if(guestToken == null) {
            return "redirect:/guest/login";
        }

        try {
            RefundResponseDto detail = refundUserClient.getRefundDetails(null, guestToken, orderId, refundId);
            model.addAttribute("detail", detail);
            return "user";
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
                                    @CookieValue(name = "guestOrderToken", required = false) String guestToken,
                                    RedirectAttributes ra) {

        if(guestToken == null) {
            return "redirect:/guest/login";
        }

        try {
            refundUserClient.cancelRefund(null, guestToken,  orderId, refundId);
            ra.addFlashAttribute("message", "비회원 반품 신청이 취소되었습니다.");
        } catch (Exception e) {
            log.error("비회원 반품 취소 실패. orderId={}, refundId={}", orderId, refundId, e);
            ra.addFlashAttribute("error", "비회원 반품 취소에 실패했습니다.");
        }
        return "redirect:/guest/refunds/orders/" + orderId + "/" + refundId;
    }
}
