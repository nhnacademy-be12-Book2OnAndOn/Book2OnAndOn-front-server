package com.nhnacademy.book2onandonfrontservice.controller.pointController;

import com.nhnacademy.book2onandonfrontservice.client.PointUserClient;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.CurrentPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointHistoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/user/me/points")
@RequiredArgsConstructor
public class PointUserController {

    private final PointUserClient pointUserClient;

    @GetMapping
    public String viewMyPointHistory(@CookieValue(value = "accessToken", required = false) String accessToken,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     Model model) {
        // 0) 페이지 번호 검증
        page = Math.max(0, page);
        size = size <= 0 ? 10 : size;

        // 1) 내 포인트 이력 조회 (백엔드 point-service 호출)
        Page<PointHistoryResponseDto> historyPage =
                pointUserClient.getMyPointHistory("Bearer " + accessToken, page, size);

        // 2) 내 현재 포인트 조회
        CurrentPointResponseDto currentPoint =
                pointUserClient.getMyCurrentPoint("Bearer " + accessToken);

        // 3) 화면에 전달할 모델 구성
        model.addAttribute("currentPoint", currentPoint);            // 현재 포인트
        model.addAttribute("histories", historyPage.getContent());   // 이력 리스트
        model.addAttribute("currentPage", page);                     // 현재 페이지(0-based)
        model.addAttribute("totalPages", historyPage.getTotalPages());

        // int startPage = Math.max(0, page - 2);
        // int endPage = Math.min(historyPage.getTotalPages() - 1, page + 2);
        // model.addAttribute("startPage", startPage);
        // model.addAttribute("endPage", endPage);

        return "user/mypage/point-history-user";
    }
}
