package controller.bookcontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nhnacademy.book2onandonfrontservice.Book2OnAndOnFrontServiceApplication;
import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.controller.GlobalControllerAdvice;
import com.nhnacademy.book2onandonfrontservice.controller.bookController.BookApiController;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookLikeToggleResponse;
import com.nhnacademy.book2onandonfrontservice.interceptor.AdminInterceptor;
import com.nhnacademy.book2onandonfrontservice.util.JwtUtils;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = BookApiController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = GlobalControllerAdvice.class
    )
)
@ContextConfiguration(classes = Book2OnAndOnFrontServiceApplication.class)
@WithMockUser
class BookApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AdminInterceptor adminInterceptor;
    @MockitoBean private BookClient bookClient;

    private MockedStatic<JwtUtils> mockedJwtUtils;
    private final Cookie authCookie = new Cookie("accessToken", "test-token");

    @BeforeEach
    void setUp() throws Exception {
        mockedJwtUtils = mockStatic(JwtUtils.class);
        given(adminInterceptor.preHandle(any(), any(), any())).willReturn(true);
    }

    @AfterEach
    void tearDown() {
        mockedJwtUtils.close();
    }

    @Test
    @DisplayName("도서 좋아요 토글 성공")
    void toggleLike_Success() throws Exception {
        mockedJwtUtils.when(() -> JwtUtils.getUserId(anyString())).thenReturn(1L);
        given(bookClient.toggleLike(anyString(), eq(1L), eq(100L)))
                .willReturn(new BookLikeToggleResponse(true, 10));

        mockMvc.perform(post("/front/books/100/likes")
                        .cookie(authCookie)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true))
                .andExpect(jsonPath("$.likeCount").value(10));
    }

    @Test
    @DisplayName("토큰이 없어 좋아요 토글 시 401 에러 반환")
    void toggleLike_NoToken_Unauthorized() throws Exception {
        mockMvc.perform(post("/front/books/100/likes")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("유효하지 않은 토큰(userId 추출 실패)인 경우 401 에러 반환")
    void toggleLike_InvalidToken_Unauthorized() throws Exception {
        mockedJwtUtils.when(() -> JwtUtils.getUserId(anyString())).thenReturn(null);

        mockMvc.perform(post("/front/books/100/likes")
                        .cookie(authCookie)
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인 정보가 올바르지 않습니다."));
    }

    @Test
    @DisplayName("Authorization 헤더를 통한 좋아요 토글 성공")
    void toggleLike_WithHeader_Success() throws Exception {
        mockedJwtUtils.when(() -> JwtUtils.getUserId(anyString())).thenReturn(1L);
        given(bookClient.toggleLike(anyString(), anyLong(), anyLong()))
                .willReturn(new BookLikeToggleResponse(false, 9));

        mockMvc.perform(post("/front/books/100/likes")
                        .header("Authorization", "Bearer header-token")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(false));
    }

    @Test
    @DisplayName("인기 도서 목록 조회 성공")
    void getPopularBooks_Success() throws Exception {
        given(bookClient.getPopularBooks(any(), anyInt(), anyInt())).willReturn(null);

        mockMvc.perform(get("/front/books/popular")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("좋아요 요청 중 Feign 401 예외 발생 시 로그인 필요 메시지 반환")
    void toggleLike_FeignUnauthorized_ReturnMessage() throws Exception {
        mockedJwtUtils.when(() -> JwtUtils.getUserId(anyString())).thenReturn(1L);

        feign.Request dummyRequest = feign.Request.create(
                feign.Request.HttpMethod.POST,
                "/url",
                java.util.Collections.emptyMap(),
                null,
                null,
                null
        );

        feign.FeignException.Unauthorized authException =
                new feign.FeignException.Unauthorized("Unauthorized", dummyRequest, null, null);

        given(bookClient.toggleLike(anyString(), anyLong(), anyLong()))
                .willThrow(authException);

        mockMvc.perform(post("/front/books/100/likes")
                        .cookie(authCookie)
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("인기 도서 조회 시 일반 예외 발생 처리")
    void getPopularBooks_InternalError() throws Exception {
        given(bookClient.getPopularBooks(any(), anyInt(), anyInt())).willThrow(new RuntimeException());

        mockMvc.perform(get("/front/books/popular"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("인기 도서 조회 실패"));
    }
}