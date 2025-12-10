package com.nhnacademy.book2onandonfrontservice.controller.pointController;

import com.nhnacademy.book2onandonfrontservice.client.PointUserClient;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.CurrentPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.ExpiringPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointHistoryResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointSummaryResponseDto;
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
        if (accessToken == null) {
            return "redirect:/login";
        }
        page = Math.max(0, page);
        size = size <= 0 ? 10 : size;

        String bearer = "Bearer " + accessToken;
        Page<PointHistoryResponseDto> historyPage = pointUserClient.getMyPointHistory(bearer, page, size);
        CurrentPointResponseDto currentPoint = pointUserClient.getMyCurrentPoint(bearer);
        PointSummaryResponseDto summary = pointUserClient.getPointSummary(bearer);
        ExpiringPointResponseDto expiring = pointUserClient.getExpiringPoints(bearer, 7);

        model.addAttribute("currentPoint", currentPoint);
        model.addAttribute("histories", historyPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", historyPage.getTotalPages());
        model.addAttribute("summary", summary); // 이번달 적립/사용
        model.addAttribute("expiring", expiring); // 소멸 예정

        // int startPage = Math.max(0, page - 2);
        // int endPage = Math.min(historyPage.getTotalPages() - 1, page + 2);
        // model.addAttribute("startPage", startPage);
        // model.addAttribute("endPage", endPage);

        return "user/mypage/point-history-user";
    }
}
