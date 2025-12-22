package com.nhnacademy.book2onandonfrontservice.controller.bookController;

import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookLikeToggleResponse;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import com.nhnacademy.book2onandonfrontservice.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/front/books")
@RequiredArgsConstructor
public class BookApiController {

    private final BookClient bookClient;

    @PostMapping("/{bookId}/likes")
    public ResponseEntity<?> toggleLike(HttpServletRequest request,
                                        @PathVariable Long bookId) {
        String accessToken = CookieUtils.getCookieValue(request, "accessToken");
        if ((accessToken == null || accessToken.isBlank())) {
            // 브라우저가 Authorization 헤더를 보낸 경우도 함께 허용 (프록시/테스트 시나리오)
            String headerToken = request.getHeader("Authorization");
            if (headerToken != null && !headerToken.isBlank()) {
                accessToken = headerToken.startsWith("Bearer ") ? headerToken.substring(7) : headerToken;
            }
        }

        if (accessToken == null || accessToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String rawToken = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        Long userId = JwtUtils.getUserId(rawToken);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 정보가 올바르지 않습니다.");
        }

        String token = "Bearer " + rawToken;
        try {
            BookLikeToggleResponse response = bookClient.toggleLike(token, userId, bookId);
            return ResponseEntity.ok(response);
        } catch (FeignException e) {
            int status = e.status();
            log.error("좋아요 요청 실패: {}", e.getMessage());
            if (status == 401 || status == 403) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("좋아요 처리에 실패했습니다.");
        } catch (Exception e) {
            log.error("좋아요 요청 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("좋아요 처리에 실패했습니다.");
        }
    }

    @GetMapping("/popular")
    public ResponseEntity<?> getPopularBooks(HttpServletRequest request,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        String accessToken = CookieUtils.getCookieValue(request, "accessToken");
        String token = accessToken == null ? null : "Bearer " + accessToken;
        try {
            return ResponseEntity.ok(bookClient.getPopularBooks(token, page, size));
        } catch (FeignException e) {
            log.error("인기 도서 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("인기 도서 조회 실패");
        } catch (Exception e) {
            log.error("인기 도서 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("인기 도서 조회 실패");
        }
    }
}
