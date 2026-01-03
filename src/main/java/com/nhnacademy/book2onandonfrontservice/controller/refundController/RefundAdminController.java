package com.nhnacademy.book2onandonfrontservice.controller.refundController;

import com.nhnacademy.book2onandonfrontservice.client.RefundAdminClient;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundSearchCondition;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundStatusUpdateRequestDto;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/admin/refunds")
public class RefundAdminController {

    private final RefundAdminClient refundAdminClient;

    // 관리자: 반품 목록(검색)
    // GET /admin/refunds?...
    @GetMapping
    public String list(@ModelAttribute RefundSearchCondition condition,
                       @PageableDefault(size = 20) Pageable pageable,
                       HttpServletRequest request,
                       Model model) {

//        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");
        String token = CookieUtils.getCookieValue(request, "accessToken");
        if (token != null && !token.startsWith("Bearer ")) token = "Bearer " + token;

        Page<RefundResponseDto> page = Page.empty(pageable);
        try {
            page = refundAdminClient.getRefundList(token, condition, pageable);
        } catch (Exception e) {
            log.error("관리자 반품 목록 조회 실패", e);
        }

        model.addAttribute("page", page);
        model.addAttribute("refunds", page.getContent());
        model.addAttribute("condition", condition);
        return "refund/admin/list";
    }

    // 관리자: 반품 상세
    @GetMapping("/{refundId}")
    public String detail(@PathVariable Long refundId,
                         HttpServletRequest request,
                         Model model,
                         RedirectAttributes ra) {

        String token = CookieUtils.getCookieValue(request, "accessToken");
        if (token != null && !token.startsWith("Bearer ")) token = "Bearer " + token;

        try {
            RefundResponseDto detail = refundAdminClient.findRefundDetails(token, refundId);
            model.addAttribute("detail", detail);
            model.addAttribute("updateRequest", new RefundStatusUpdateRequestDto());
            return "refund/admin/detail";
        } catch (Exception e) {
            log.error("관리자 반품 상세 조회 실패. refundId={}", refundId, e);
            ra.addFlashAttribute("error", "반품 상세를 불러오지 못했습니다.");
            return "redirect:/admin/refunds";
        }
    }

    // 관리자: 상태 변경(폼 submit)
    // POST /admin/refunds/{refundId}/status
    @PostMapping("/{refundId}/status")
    public String updateStatus(@PathVariable Long refundId,
                               @ModelAttribute RefundStatusUpdateRequestDto requestDto,
                               HttpServletRequest request,
                               RedirectAttributes ra) {

        String token = CookieUtils.getCookieValue(request, "accessToken");
        if (token != null && !token.startsWith("Bearer ")) token = "Bearer " + token;

        try {
            refundAdminClient.updateRefundStatus(token, refundId, requestDto);
            ra.addFlashAttribute("message", "반품 상태가 변경되었습니다.");
        } catch (Exception e) {
            log.error("관리자 반품 상태 변경 실패. refundId={}", refundId, e);
            ra.addFlashAttribute("error", "상태 변경에 실패했습니다.");
        }

        return "redirect:/admin/refunds/" + refundId;
    }
}
