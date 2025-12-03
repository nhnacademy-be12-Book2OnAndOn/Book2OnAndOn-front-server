package com.nhnacademy.book2onandonfrontservice.controller.pointController;

import com.nhnacademy.book2onandonfrontservice.client.PointAdminClient;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.CurrentPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointHistoryAdminAdjustRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointHistoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/points")
@RequiredArgsConstructor
public class PointAdminController {

    private final PointAdminClient pointAdminClient;

    @GetMapping
    public String listUserPointHistory(@RequestParam Long userId,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       Model model) {
        // 0) 페이지 번호 검증
        page = Math.max(0, page);
        size = size <= 0 ? 10 : size;

        // 1) 포인트 이력 페이지 조회
        Page<PointHistoryResponseDto> historyPage =
                pointAdminClient.getUserPointHistory(userId, page, size);

        // 2) 현재 보유 포인트 조회
        CurrentPointResponseDto currentPoint =
                pointAdminClient.getUserCurrentPoint(userId);

        // 3) 뷰에 전달
        model.addAttribute("userId", userId);
        model.addAttribute("currentPoint", currentPoint);
        model.addAttribute("histories", historyPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", historyPage.getTotalPages());

        return "/user/mypage/point-history-admin";
    }

    @PostMapping("/adjust")
    public String adjustUserPoint(@ModelAttribute PointHistoryAdminAdjustRequestDto requestDto) {

        // 실제 포인트 조정 호출
        pointAdminClient.adjustPointByAdmin(requestDto);

        // 조정 후 다시 해당 회원 이력 화면으로 리다이렉트
        return "redirect:/admin/points?userId=" + requestDto.getUserId();
    }

    @PostMapping("/expire")
    public String expireUserPoints(@RequestParam Long userId) {

        pointAdminClient.expirePoints(userId);

        return "redirect:/admin/points?userId=" + userId;
    }
}
