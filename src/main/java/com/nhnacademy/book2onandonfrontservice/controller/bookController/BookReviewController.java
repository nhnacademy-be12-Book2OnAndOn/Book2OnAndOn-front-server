package com.nhnacademy.book2onandonfrontservice.controller.bookController;

import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.ReviewCreateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.ReviewDto;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.ReviewUpdateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.BookReviewResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.UserResponseDto;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import feign.FeignException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookReviewController {
    private final BookClient bookClient;
    private final UserClient userClient;

    /**
     * 리뷰 등록 처리
     * [POST] /{bookId}/reviews
     */
    @PostMapping("/{bookId}/reviews")
    public String createReview(@PathVariable Long bookId,
                               @ModelAttribute @Valid ReviewCreateRequest request,
                               @RequestParam(value = "images", required = false) List<MultipartFile> images,
                               HttpServletRequest servletRequest) {
        String token = toBearer(CookieUtils.getCookieValue(servletRequest, "accessToken"));

        if (token == null) {
            return "redirect:/login";
        }

        try {
            UserResponseDto myInfo = userClient.getMyInfo(token);
            request.setWriterName(myInfo.getNickname() == null ? myInfo.getName() : myInfo.getNickname());

            bookClient.createReview( token,bookId, request, images);
            log.debug("ReviewCreateRequest title: {}", request.getTitle());
        } catch (FeignException e) {
            HttpStatus status = HttpStatus.resolve(e.status());
            String code = status != null ? status.toString() : "UNKNOWN";
            log.error("리뷰 등록 실패(status={}): {}", code, e.getMessage());
            return "redirect:/books/" + bookId + "?error=review_failed&code=" + e.status();
        } catch (Exception e) {
            log.error("리뷰 등록 실패", e);
            return "redirect:/books/" + bookId + "?error=review_failed";
        }
        return "redirect:/books/" + bookId;
    }

    /**
     * 리뷰 수정 처리
     * [POST] /reviews/{reviewId}/update
     */
    @PostMapping("/reviews/{reviewId}")
    public String updateReview(@PathVariable Long reviewId,
                               @ModelAttribute @Valid ReviewUpdateRequest request,
                               @RequestParam(value = "images", required = false) List<MultipartFile> images,
                               HttpServletRequest servletRequest) {

        String token = toBearer(CookieUtils.getCookieValue(servletRequest, "accessToken"));

        if (token == null) {
            return "redirect:/login";
        }

        try {
            bookClient.updateReview( token,reviewId, request, images);

        } catch (Exception e) {
            log.error("리뷰 수정 실패", e);
            return "redirect:/users/me/reviews" + "?error=review_update_failed";
        }

        return "redirect:/users/me/reviews";
    }

    @GetMapping("/reviews/{reviewId}")
    public String editReviewForm(@PathVariable Long reviewId,
                                 @RequestParam Long bookId, // 취소 시 돌아갈 곳
                                 HttpServletRequest servletRequest,
                                 Model model) {

        String token = toBearer(CookieUtils.getCookieValue(servletRequest, "accessToken"));

        if (token == null) {
            return "redirect:/login";
        }

        ReviewDto review = bookClient.getReview(token, reviewId);

        model.addAttribute("review", review);
        model.addAttribute("bookId", bookId);

        return "user/mypage/review-edit"; // 별도 수정 페이지 HTML
    }

    private String toBearer(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return null;
        }
        String decoded = accessToken;
        try {
            decoded = URLDecoder.decode(accessToken, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }
        return decoded.startsWith("Bearer ") ? decoded : "Bearer " + decoded;
    }
}
