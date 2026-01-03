package com.nhnacademy.book2onandonfrontservice.controller.refundController;

import com.nhnacademy.book2onandonfrontservice.client.RefundUserClient;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.RefundAvailableItemResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.RefundResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundRequestDto;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/refunds") // 프론트 URL prefix (예시)
public class RefundUserController {

    private final RefundUserClient refundUserClient;

    /**
     * 회원: 내 반품 목록
     * GET /refunds/my?page=0&size=10
     */
    @GetMapping("/my")
    public String myRefunds(HttpServletRequest request,
                            @PageableDefault(size = 10) Pageable pageable,
                            Model model) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");

        Page<RefundResponseDto> page = Page.empty(pageable);
        try {
            page = refundUserClient.getMyRefunds(token, pageable); // Feign 경로는 /api/... 로 수정된 상태 가정
        } catch (Exception e) {
            log.error("회원 반품 목록 조회 실패", e);
        }

        model.addAttribute("page", page);
        model.addAttribute("refunds", page.getContent());
        return "refund/user/my_list"; // templates/refund/user/my_list.html
    }

    /**
     * 회원: 반품 신청 폼
     * GET /refunds/orders/{orderId}/form
     */
    @GetMapping("/orders/{orderId}/form")
    public String refundForm(@PathVariable Long orderId,
                             HttpServletRequest request,
                             Model model,
                             RedirectAttributes ra) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");

        try {
            List<RefundAvailableItemResponseDto> items = refundUserClient.getRefundForm(token, null, orderId);
            model.addAttribute("orderId", orderId);
            model.addAttribute("items", items);
            model.addAttribute("refundRequest", new RefundRequestDto());
            return "refund/user/form";
        } catch (Exception e) {
            log.error("회원 반품 폼 조회 실패. orderId={}", orderId, e);
            ra.addFlashAttribute("error", "반품 신청 폼을 불러오지 못했습니다.");
            return "redirect:/orders/" + orderId; // 너희 주문상세 페이지로 조정
        }
    }

    /**
     * 회원: 반품 신청 제출
     * POST /refunds/orders/{orderId}
     */
    @PostMapping("/orders/{orderId}")
    public String submitRefund(@PathVariable Long orderId,
                               HttpServletRequest request,
                               @ModelAttribute RefundRequestDto refundRequestDto,
                               RedirectAttributes ra) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");

        try {
            RefundResponseDto created = refundUserClient.createRefund(token, null, orderId, refundRequestDto);
            ra.addFlashAttribute("message", "반품 신청이 접수되었습니다.");
            return "redirect:/refunds/orders/" + orderId + "/" + created.getRefundId();
        } catch (Exception e) {
            log.error("회원 반품 신청 실패. orderId={}", orderId, e);
            ra.addFlashAttribute("error", "반품 신청에 실패했습니다.");
            return "redirect:/refunds/orders/" + orderId + "/form";
        }
    }

    /**
     * 회원: 반품 상세
     * GET /refunds/orders/{orderId}/{refundId}
     */
    @GetMapping("/orders/{orderId}/{refundId}")
    public String refundDetail(@PathVariable Long orderId,
                               @PathVariable Long refundId,
                               HttpServletRequest request,
                               Model model,
                               RedirectAttributes ra) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");

        try {
            RefundResponseDto detail = refundUserClient.getRefundDetails(token, null, orderId, refundId);
            model.addAttribute("detail", detail);
            return "refund/user/detail";
        } catch (Exception e) {
            log.error("회원 반품 상세 조회 실패. orderId={}, refundId={}", orderId, refundId, e);
            ra.addFlashAttribute("error", "반품 상세를 불러오지 못했습니다.");
            return "redirect:/refunds/my";
        }
    }

    /**
     * 회원: 반품 신청 취소
     * POST /refunds/orders/{orderId}/{refundId}/cancel
     */
    @PostMapping("/orders/{orderId}/{refundId}/cancel")
    public String cancelRefund(@PathVariable Long orderId,
                               @PathVariable Long refundId,
                               HttpServletRequest request,
                               RedirectAttributes ra) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");

        try {
            refundUserClient.cancelRefund(token, null, orderId, refundId);
            ra.addFlashAttribute("message", "반품 신청이 취소되었습니다.");
        } catch (Exception e) {
            log.error("회원 반품 취소 실패. orderId={}, refundId={}", orderId, refundId, e);
            ra.addFlashAttribute("error", "반품 취소에 실패했습니다.");
        }
        return "redirect:/refunds/orders/" + orderId + "/" + refundId;
    }
}
