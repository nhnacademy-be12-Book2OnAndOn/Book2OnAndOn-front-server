package com.nhnacademy.book2onandonfrontservice.controller.pointController;

import com.nhnacademy.book2onandonfrontservice.client.PointAdminClient;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.CurrentPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointHistoryAdminAdjustRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointHistoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/points")
@RequiredArgsConstructor
public class PointAdminController {

    private final PointAdminClient pointAdminClient;

    @GetMapping
    public String listUserPointHistory(@CookieValue(value = "accessToken", required = false) String accessToken,
                                       @RequestParam Long userId,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       Model model) {
        // 0) 페이지 번호 검증
        page = Math.max(0, page);
        size = size <= 0 ? 10 : size;

        // 1) 포인트 이력 페이지 조회
        Page<PointHistoryResponseDto> historyPage =
                pointAdminClient.getUserPointHistory("Bearer " + accessToken, userId, page, size);

        // 2) 현재 보유 포인트 조회
        CurrentPointResponseDto currentPoint =
                pointAdminClient.getUserCurrentPoint("Bearer " + accessToken, userId);

        // 3) 뷰에 전달
        model.addAttribute("userId", userId);
        model.addAttribute("currentPoint", currentPoint);
        model.addAttribute("histories", historyPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", historyPage.getTotalPages());

        return "/user/mypage/point-history-admin";
    }

    @PostMapping("/adjust")
    public String adjustUserPoint(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @ModelAttribute PointHistoryAdminAdjustRequestDto requestDto) {

        if (accessToken == null) {
            return "redirect:/login";
        }

        pointAdminClient.adjustPointByAdmin("Bearer " + accessToken, requestDto);

        return "redirect:/admin/points?userId=" + requestDto.getUserId();
    }

    @PostMapping("/expire")
    public String expireUserPoints(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam Long userId) {

        if (accessToken == null) {
            return "redirect:/login";
        }

        pointAdminClient.expirePoints("Bearer " + accessToken);

        return "redirect:/admin/points?userId=" + userId;
    }
}
