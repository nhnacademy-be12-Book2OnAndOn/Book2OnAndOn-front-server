package com.nhnacademy.book2onandonfrontservice.controller.refundController;

import com.nhnacademy.book2onandonfrontservice.client.RefundClient;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.*;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping
public class RefundController {

    private final RefundClient refundClient;

    private static final Map<String, String> REFUND_REASON_LABELS = Map.of(
            "PRODUCT_DEFECT", "상품 불량",
            "WRONG_DELIVERY", "오배송",
            "CHANGE_OF_MIND", "단순 변심",
            "OTHER", "기타"
    );

    /* ================= 공통 유틸 ================= */

    private String bearer(HttpServletRequest request) {
        String token = CookieUtils.getCookieValue(request, "accessToken");
        if (token == null || token.isBlank()) return null;
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }

    private String guestToken(HttpServletRequest request) {
        return CookieUtils.getCookieValue(request, "guestOrderToken");
    }

    /* ================= 회원 전용 ================= */

    /**
     * 회원: 내 반품 목록
     * GET /users/me/refunds
     */
    @GetMapping("/users/me/refunds")
    public String myRefunds(HttpServletRequest request,
                            @PageableDefault(size = 10, sort = "refundCreatedAt", direction = Sort.Direction.DESC) Pageable pageable,
                            Model model) {

        String accessToken = bearer(request);

        Page<RefundResponseDto> page = refundClient.getMyRefunds(accessToken, pageable);
        model.addAttribute("page", page);
        model.addAttribute("refunds", page.getContent());
        model.addAttribute("refundReasonLabels", REFUND_REASON_LABELS);
        return "refund/refund-mypage";
    }

    /* ================= 회원/비회원 공통 ================= */

    /**
     * 반품 신청 폼
     * GET /refunds/orders/{orderId}/form
     */
    @GetMapping("/orders/{orderId}/refunds/form")
    public String refundForm(@PathVariable Long orderId,
                             HttpServletRequest request,
                             Model model,
                             RedirectAttributes ra) {

        String accessToken = bearer(request);
        String guestToken = guestToken(request);

        if (accessToken == null && guestToken == null) {
            return "redirect:/login";
        }

        try {
            List<RefundAvailableItemResponseDto> items =
                    refundClient.getRefundForm(accessToken, guestToken, orderId);

            model.addAttribute("orderId", orderId);
            model.addAttribute("items", items);
            model.addAttribute("refundRequest", new RefundRequestDto());
            model.addAttribute("userType", accessToken != null ? "member" : "guest");
            return "refund/refund-form";

        } catch (Exception e) {
            log.error("반품 신청 폼 조회 실패", e);
            ra.addFlashAttribute("errorMessage", "반품 신청 폼을 불러오지 못했습니다.");
            return "redirect:/orders/" + orderId;
        }
    }

    /**
     * 반품 신청
     * POST /refunds/orders/{orderId}
     */
    @PostMapping("/orders/{orderId}/refunds")
    public String submitRefund(@PathVariable Long orderId,
                               HttpServletRequest request,
                               @ModelAttribute RefundRequestDto refundRequestDto,
                               RedirectAttributes ra) {

        String accessToken = bearer(request);
        String guestToken = guestToken(request);

        try {
            RefundResponseDto created =
                    refundClient.createRefund(accessToken, guestToken, orderId, refundRequestDto);

            ra.addFlashAttribute("message", "반품 신청이 접수되었습니다.");
            return "redirect:/orders/" + orderId + "/refunds/" + created.getRefundId();

        } catch (Exception e) {
            log.error("반품 신청 실패", e);
            ra.addFlashAttribute("errorMessage", "반품 신청에 실패했습니다.");
            return "redirect:/orders/" + orderId + "/refunds/form";
        }
    }

    /**
     * 반품 상세
     * GET /refunds/orders/{orderId}/{refundId}
     */
    @GetMapping("/orders/{orderId}/refunds/{refundId}")
    public String refundDetail(@PathVariable Long orderId,
                               @PathVariable Long refundId,
                               HttpServletRequest request,
                               Model model,
                               RedirectAttributes ra) {

        String accessToken = bearer(request);
        String guestToken = guestToken(request);

        try {
            RefundResponseDto detail =
                    refundClient.getRefundDetails(accessToken, guestToken, orderId, refundId);

            model.addAttribute("refund", detail);
            model.addAttribute("userType", accessToken != null ? "member" : "guest");
            model.addAttribute("refundReasonLabels", REFUND_REASON_LABELS);
            log.info("refundDetail dto refundId={}", detail.getRefundId());
            return "refund/refund-detail";

        } catch (Exception e) {
            log.error("반품 상세 조회 실패", e);
            ra.addFlashAttribute("errorMessage", "반품 상세를 불러오지 못했습니다.");
            return "redirect:/users/me/refunds";
        }
    }

    /**
     * 반품 취소
     * POST /refunds/orders/{orderId}/{refundId}/cancel
     */
    @PostMapping("/orders/{orderId}/refunds/{refundId}/cancel")
    public String cancelRefund(@PathVariable Long orderId,
                               @PathVariable Long refundId,
                               HttpServletRequest request,
                               RedirectAttributes ra) {

        String accessToken = bearer(request);
        String guestToken = guestToken(request);

        try {
            refundClient.cancelRefund(accessToken, guestToken, orderId, refundId);
            ra.addFlashAttribute("message", "반품 신청이 취소되었습니다.");
        } catch (Exception e) {
            log.error("반품 취소 실패", e);
            ra.addFlashAttribute("errorMessage", "반품 취소에 실패했습니다.");
        }

        return "redirect:/orders/" + orderId + "/refunds/" + refundId;
    }
}
