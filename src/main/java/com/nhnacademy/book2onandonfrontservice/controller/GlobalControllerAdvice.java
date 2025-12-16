package com.nhnacademy.book2onandonfrontservice.controller;

import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDto;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookSearchCondition;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.CategoryDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.UserResponseDto;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// 뷰에 로그인 정보 전달
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {
    private final UserClient userClient;
    private final BookClient bookClient;

    @ModelAttribute("user")
    public UserResponseDto addUserToModel(HttpServletRequest request) {
        String accessToken = CookieUtils.getCookieValue(request, "accessToken");

        if (accessToken != null) {
            try {
                return userClient.getMyInfo("Bearer " + accessToken);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @ModelAttribute("categories")
    public List<CategoryDto> addCategories() {
        try {
            return bookClient.getCategories();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /*
    최근 본 상품 플로팅 바에 사용됨
     */
    @ModelAttribute("recentBooks")
    public List<BookDto> addRecentViews(HttpServletRequest request){
        try{
            String guestId = CookieUtils.getCookieValue(request, "GUEST_ID");
            String accessToken = CookieUtils.getCookieValue(request, "accessToken");

            if (accessToken != null && guestId != null
                    && request.getSession().getAttribute("recentViewsMergeAttempted") == null) {
                try {
                    bookClient.mergeRecentViews(toBearer(accessToken), guestId);
                } catch (Exception e) {
                    log.warn("최근 본 도서 병합 실패", e);
                } finally {
                    request.getSession().setAttribute("recentViewsMergeAttempted", Boolean.TRUE);
                }
            }

            if(guestId == null && accessToken==null){
                return Collections.emptyList();
            }
            return bookClient.getRecentViews(toBearer(accessToken), guestId);
        }catch (Exception e){
            log.warn("최근 본 도서 조회 실패", e);
            return Collections.emptyList();
        }
    }

    /// 검색 엔진
    @ModelAttribute("condition")
    public BookSearchCondition addSearchCondition(){
        return new BookSearchCondition();
    }

    private String toBearer(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return null;
        }
        return accessToken.startsWith("Bearer ") ? accessToken : "Bearer " + accessToken;
    }
}
