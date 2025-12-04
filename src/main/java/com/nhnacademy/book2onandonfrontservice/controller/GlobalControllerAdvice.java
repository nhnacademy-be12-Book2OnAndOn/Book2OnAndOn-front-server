package com.nhnacademy.book2onandonfrontservice.controller;

import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.CategoryDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.UserResponseDto;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// 뷰에 로그인 정보 전달
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
}
