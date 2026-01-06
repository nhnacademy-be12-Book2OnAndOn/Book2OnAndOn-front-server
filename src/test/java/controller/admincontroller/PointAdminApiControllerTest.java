package controller.admincontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.book2onandonfrontservice.Book2OnAndOnFrontServiceApplication;
import com.nhnacademy.book2onandonfrontservice.client.PointAdminClient;
import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.controller.GlobalControllerAdvice;
import com.nhnacademy.book2onandonfrontservice.controller.adminController.PointAdminApiController;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.CurrentPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.EarnPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointHistoryAdminAdjustRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.UserResponseDto;
import com.nhnacademy.book2onandonfrontservice.interceptor.AdminInterceptor;
import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = PointAdminApiController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = GlobalControllerAdvice.class
    )
)
@ContextConfiguration(classes = Book2OnAndOnFrontServiceApplication.class)
@WithMockUser(roles = "ADMIN")
class PointAdminApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private AdminInterceptor adminInterceptor;
    @MockitoBean private PointAdminClient pointAdminClient;
    @MockitoBean private UserClient userClient;

    private final Cookie authCookie = new Cookie("accessToken", "test-token");

    @BeforeEach
    void setUp() throws Exception {
        given(adminInterceptor.preHandle(any(), any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("사용자 포인트 이력 조회 성공")
    void getUserPointHistory_Success() throws Exception {
        given(userClient.getUserDetail(anyString(), anyLong())).willReturn(new UserResponseDto());
        given(pointAdminClient.getUserPointHistory(anyString(), anyLong(), anyInt(), anyInt()))
                .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/admin/api/points")
                        .param("userId", "1")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("사용자 현재 포인트 조회 성공")
    void getCurrentPoint_Success() throws Exception {
        given(userClient.getUserDetail(anyString(), anyLong())).willReturn(new UserResponseDto());
        given(pointAdminClient.getUserCurrentPoint(anyString(), anyLong()))
                .willReturn(new CurrentPointResponseDto(1000));

        mockMvc.perform(get("/admin/api/points/current")
                        .param("userId", "1")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPoint").value(1000));
    }

    @Test
    @DisplayName("포인트 수동 조정 성공")
    void adjustPoint_Success() throws Exception {
        PointHistoryAdminAdjustRequestDto requestDto = new PointHistoryAdminAdjustRequestDto(
                1L,
                500,
                "관리자 조정",
                "EARN",
                null
        );        given(pointAdminClient.adjustPointByAdmin(anyString(), any())).willReturn(mock(EarnPointResponseDto.class));

        mockMvc.perform(post("/admin/api/points/adjust")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(authCookie)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("준비되지 않은 포인트 만료 API 호출 시 501 응답")
    void expirePoints_NotImplemented() throws Exception {
        mockMvc.perform(post("/admin/api/points/expire")
                        .with(csrf()))
                .andExpect(status().isNotImplemented());
    }

    @Test
    @DisplayName("인증 쿠키 누락 시 401 응답")
    void auth_MissingCookie_Unauthorized() throws Exception {
        mockMvc.perform(get("/admin/api/points").param("userId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 404 응답")
    void validateUser_NotFound() throws Exception {
        // validateUser 내부에서 발생하는 FeignException.NotFound 시뮬레이션
        given(userClient.getUserDetail(anyString(), anyLong())).willThrow(feign.FeignException.NotFound.class);

        mockMvc.perform(get("/admin/api/points/current")
                        .param("userId", "999")
                        .cookie(authCookie))
                .andExpect(status().isNotFound());
    }
}
