package com.nhnacademy.book2onandonfrontservice.controller.adminController;

import com.nhnacademy.book2onandonfrontservice.client.PointAdminClient;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.CurrentPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.EarnPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointHistoryAdminAdjustRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointHistoryResponseDto;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/points")
public class PointAdminApiController {

    private final PointAdminClient pointAdminClient;

    public PointAdminApiController(PointAdminClient pointAdminClient) {
        this.pointAdminClient = pointAdminClient;
    }

    @GetMapping
    public ResponseEntity<?> getUserPointHistory(HttpServletRequest request,
                                                 @RequestParam Long userId,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        String accessToken = bearer(CookieUtils.getCookieValue(request, "accessToken"));
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            Page<PointHistoryResponseDto> history = pointAdminClient.getUserPointHistory(accessToken, userId, page, size);
            Map<String, Object> payload = new HashMap<>();
            payload.put("content", history.getContent());
            payload.put("totalPages", history.getTotalPages());
            payload.put("totalElements", history.getTotalElements());
            payload.put("number", history.getNumber());
            payload.put("size", history.getSize());
            return ResponseEntity.ok(payload);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("포인트 이력 조회에 실패했습니다.");
        }
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentPoint(HttpServletRequest request,
                                             @RequestParam Long userId) {
        String accessToken = bearer(CookieUtils.getCookieValue(request, "accessToken"));
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            CurrentPointResponseDto current = pointAdminClient.getUserCurrentPoint(accessToken, userId);
            return ResponseEntity.ok(current);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("현재 포인트 조회에 실패했습니다.");
        }
    }

    @PostMapping("/adjust")
    public ResponseEntity<?> adjustPoint(HttpServletRequest request,
                                         @RequestBody PointHistoryAdminAdjustRequestDto requestDto) {
        String accessToken = bearer(CookieUtils.getCookieValue(request, "accessToken"));
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            EarnPointResponseDto res = pointAdminClient.adjustPointByAdmin(accessToken, requestDto);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("포인트 조정에 실패했습니다.");
        }
    }

    @PostMapping("/expire")
    public ResponseEntity<?> expirePoints() {
        // 백엔드 만료 API 없음: 프런트 버튼 대응용.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("만료 포인트 처리 API가 준비되지 않았습니다.");
    }

    private String bearer(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}
