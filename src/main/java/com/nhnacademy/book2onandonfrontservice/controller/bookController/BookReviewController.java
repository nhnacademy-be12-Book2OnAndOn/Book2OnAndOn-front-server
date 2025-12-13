package com.nhnacademy.book2onandonfrontservice.controller.bookController;

import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.ReviewCreateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.ReviewUpdateRequest;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookReviewController {
    private final BookClient bookClient;

    /**
     * 리뷰 등록 처리
     * [POST] /{bookId}/reviews
     */
    @PostMapping("/{bookId}/reviews")
    public String createReview(@PathVariable Long bookId,
                               @ModelAttribute @Valid ReviewCreateRequest request,
                               @RequestParam(value = "images", required = false) List<MultipartFile> images,
                               HttpServletRequest servletRequest) {
        String token = "Bearer " + CookieUtils.getCookieValue(servletRequest, "accessToken");


        try {
            bookClient.createReview( token,bookId, request, images);

        } catch (Exception e) {
            log.error("리뷰 등록 실패", e);
            return "redirect:/books/" + bookId + "?error=review_failed";
        }
        return "redirect:/books/" + bookId;
    }

    /**
     * 리뷰 수정 처리
     * [POST] /reviews/{reviewId}/update
     * (HTML Form은 PUT을 지원하지 않으므로 POST로 받고 내부에서 처리)
     */
    @PostMapping("/reviews/{reviewId}")
    public String updateReview(@PathVariable Long reviewId,
                               @RequestParam("bookId") Long bookId, // 리다이렉트용
                               @ModelAttribute @Valid ReviewUpdateRequest request,
                               @RequestParam(value = "images", required = false) List<MultipartFile> images,
                               HttpServletRequest servletRequest) {

        String token = "Bearer " + CookieUtils.getCookieValue(servletRequest, "accessToken");

        try {
            bookClient.updateReview( token,reviewId, request, images);

        } catch (Exception e) {
            log.error("리뷰 수정 실패", e);
            return "redirect:/books/" + bookId + "?error=review_update_failed";
        }

        return "redirect:/books/" + bookId;
    }
}
