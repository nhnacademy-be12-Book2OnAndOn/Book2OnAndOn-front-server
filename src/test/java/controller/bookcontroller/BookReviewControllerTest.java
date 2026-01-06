package com.nhnacademy.book2onandonfrontservice.controller.bookController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.nhnacademy.book2onandonfrontservice.Book2OnAndOnFrontServiceApplication;
import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.controller.GlobalControllerAdvice;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.ReviewDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.UserResponseDto;
import com.nhnacademy.book2onandonfrontservice.interceptor.AdminInterceptor;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        value = BookReviewController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalControllerAdvice.class
        )
)
@ContextConfiguration(classes = Book2OnAndOnFrontServiceApplication.class)
@WithMockUser
class BookReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AdminInterceptor adminInterceptor;
    @MockitoBean private BookClient bookClient;
    @MockitoBean private UserClient userClient;

    private final Cookie authCookie = new Cookie("accessToken", "test-token");

    @BeforeEach
    void setUp() throws Exception {
        given(adminInterceptor.preHandle(any(), any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("리뷰 등록 성공 및 도서 상세 페이지로 리다이렉트")
    void createReview_Success() throws Exception {
        UserResponseDto userDto = new UserResponseDto();
        userDto.setName("홍길동");
        given(userClient.getMyInfo(anyString())).willReturn(userDto);

        MockMultipartFile image = new MockMultipartFile("images", "review.jpg", MediaType.IMAGE_JPEG_VALUE, "data".getBytes());

        mockMvc.perform(multipart("/books/100/reviews")
                        .file(image)
                        .param("title", "제목입니다 5자이상")
                        .param("content", "리뷰 내용입니다. 10자 이상 작성합니다.") // 10자 이상
                        .param("score", "5") // rating -> score로 수정
                        .cookie(authCookie)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/100"));
    }

    @Test
    @DisplayName("리뷰 등록 실패 시 에러 파라미터와 함께 리다이렉트")
    void createReview_Fail_Exception() throws Exception {
        given(userClient.getMyInfo(anyString())).willThrow(new RuntimeException());

        mockMvc.perform(multipart("/books/100/reviews")
                        .param("title", "제목입니다 5자이상")
                        .param("content", "리뷰 내용입니다. 10자 이상 작성합니다.")
                        .param("score", "5")
                        .cookie(authCookie)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/100?error=review_failed"));
    }

    @Test
    @DisplayName("리뷰 수정 성공 및 내 리뷰 목록으로 리다이렉트")
    void updateReview_Success() throws Exception {
        mockMvc.perform(multipart("/books/reviews/50")
                        .param("title", "수정된 제목입니다")
                        .param("content", "수정된 리뷰 내용입니다. 10자 이상.")
                        .param("score", "4")
                        .cookie(authCookie)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/me/reviews"));
    }

    @Test
    @DisplayName("리뷰 수정 실패 시 에러 파라미터와 함께 리다이렉트")
    void updateReview_Fail_Exception() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException())
                .when(bookClient).updateReview(anyString(), anyLong(), any(), any());

        mockMvc.perform(multipart("/books/reviews/50")
                        .param("title", "수정된 제목입니다")
                        .param("content", "수정된 리뷰 내용입니다. 10자 이상.")
                        .param("score", "4")
                        .cookie(authCookie)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/me/reviews?error=review_update_failed"));
    }

    @Test
    @DisplayName("리뷰 수정 폼 페이지 조회")
    void editReviewForm_View() throws Exception {
        ReviewDto reviewDto = new ReviewDto();
        given(bookClient.getReview(anyString(), anyLong())).willReturn(reviewDto);

        mockMvc.perform(get("/books/reviews/50")
                        .param("bookId", "100")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("user/mypage/review-edit"))
                .andExpect(model().attributeExists("review", "bookId"));
    }
}