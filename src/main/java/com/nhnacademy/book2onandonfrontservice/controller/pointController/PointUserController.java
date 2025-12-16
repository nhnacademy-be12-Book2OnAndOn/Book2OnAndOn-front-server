package com.nhnacademy.book2onandonfrontservice.controller.pointController;

import com.nhnacademy.book2onandonfrontservice.client.PointUserClient;
import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.CurrentPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.ExpiringPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointHistoryResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointSummaryResponseDto;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user/me/points")
@RequiredArgsConstructor
public class PointUserController {

    private final PointUserClient pointUserClient;

    @GetMapping
    public String viewMyPointHistory(@CookieValue(value = "accessToken", required = false) String accessToken,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(required = false) String type,
                                     Model model) {
        if (accessToken == null) {
            return "redirect:/login";
        }
        page = Math.max(0, page);
        size = size <= 0 ? 10 : size;

        String bearer = "Bearer " + accessToken;
        Page<PointHistoryResponseDto> historyPage =
                (type != null && !type.isBlank())
                        ? pointUserClient.getMyPointHistoryByType(bearer, type, page, size)
                        : pointUserClient.getMyPointHistory(bearer, page, size);
        CurrentPointResponseDto currentPoint = pointUserClient.getMyCurrentPoint(bearer);
        PointSummaryResponseDto summary = pointUserClient.getPointSummary(bearer);
        ExpiringPointResponseDto expiring = pointUserClient.getExpiringPoints(bearer, 7);

        model.addAttribute("currentPoint", currentPoint);
        model.addAttribute("histories", toHistoryViews(historyPage.getContent()));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", historyPage.getTotalPages());
        model.addAttribute("summary", summary); // 이번달 적립/사용
        model.addAttribute("expiring", expiring); // 소멸 예정
        // 필터 상태 유지용
        model.addAttribute("type", type);

        // int startPage = Math.max(0, page - 2);
        // int endPage = Math.min(historyPage.getTotalPages() - 1, page + 2);
        // model.addAttribute("startPage", startPage);
        // model.addAttribute("endPage", endPage);

        return "user/mypage/point-history-user";
    }

    @GetMapping("/api/current")
    @ResponseBody
    public ResponseEntity<CurrentPointResponseDto> getCurrentPoint(
            @CookieValue(value = "accessToken", required = false) String accessToken) {
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            CurrentPointResponseDto currentPoint =
                    pointUserClient.getMyCurrentPoint("Bearer " + accessToken);
            return ResponseEntity.ok(currentPoint);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/history")
    @ResponseBody
    public ResponseEntity<Page<PointHistoryResponseDto>> getPointHistory(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type) {

        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            page = Math.max(0, page);
            size = size <= 0 ? 10 : size;
            String bearer = "Bearer " + accessToken;
            Page<PointHistoryResponseDto> historyPage =
                    (type != null && !type.isBlank())
                            ? pointUserClient.getMyPointHistoryByType(bearer, type, page, size)
                            : pointUserClient.getMyPointHistory(bearer, page, size);
            return ResponseEntity.ok(historyPage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private List<PointHistoryView> toHistoryViews(List<PointHistoryResponseDto> histories) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return histories.stream()
                .map(h -> new PointHistoryView(
                        h.getPointHistoryId(),
                        h.getPointHistoryChange(),
                        h.getTotalPoints(),
                        h.getPointCreatedDate() != null ? h.getPointCreatedDate().format(formatter) : null,
                        h.getPointExpiredDate() != null ? h.getPointExpiredDate().format(formatter) : null,
                        h.getRemainingPoint(),
                        h.getPointReason() != null ? h.getPointReason().name() : null
                ))
                .collect(Collectors.toList());
    }

    private record PointHistoryView(
            Long pointHistoryId,
            int pointHistoryChange,
            int totalPoints,
            String pointCreatedDate,
            String pointExpiredDate,
            Integer remainingPoint,
            String pointReason
    ) { }
}
