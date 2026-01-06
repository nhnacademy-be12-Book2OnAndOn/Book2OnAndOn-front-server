package controller.pointcontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.nhnacademy.book2onandonfrontservice.Book2OnAndOnFrontServiceApplication;
import com.nhnacademy.book2onandonfrontservice.client.PointUserClient;
import com.nhnacademy.book2onandonfrontservice.controller.GlobalControllerAdvice;
import com.nhnacademy.book2onandonfrontservice.controller.pointController.PointUserController;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.PointReason;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.CurrentPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.ExpiringPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointHistoryResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointSummaryResponseDto;
import com.nhnacademy.book2onandonfrontservice.interceptor.AdminInterceptor;
import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = PointUserController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = GlobalControllerAdvice.class
    )
)
@ContextConfiguration(classes = Book2OnAndOnFrontServiceApplication.class)
@WithMockUser
class PointUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AdminInterceptor adminInterceptor;
    @MockitoBean private PointUserClient pointUserClient;

    private final Cookie authCookie = new Cookie("accessToken", "test-token");

    @BeforeEach
    void setUp() throws Exception {
        given(adminInterceptor.preHandle(any(), any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("사용자 마이페이지 포인트 내역 조회 성공")
    void viewMyPointHistory_Success() throws Exception {
        PointHistoryResponseDto history = mock(PointHistoryResponseDto.class);
        given(history.getPointReason()).willReturn(PointReason.ORDER);
        given(history.getPointCreatedDate()).willReturn(LocalDateTime.now());

        given(pointUserClient.getMyPointHistory(anyString(), anyInt(), anyInt()))
                .willReturn(new PageImpl<>(List.of(history), PageRequest.of(0, 10), 1));
        given(pointUserClient.getMyCurrentPoint(anyString())).willReturn(new CurrentPointResponseDto(1000));
        given(pointUserClient.getPointSummary(anyString())).willReturn(mock(PointSummaryResponseDto.class));
        given(pointUserClient.getExpiringPoints(anyString(), anyInt())).willReturn(mock(ExpiringPointResponseDto.class));

        mockMvc.perform(get("/user/me/points")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("user/mypage/point-history-user"))
                .andExpect(model().attributeExists("currentPoint", "histories", "summary", "expiring"));
    }

    @Test
    @DisplayName("액세스 토큰 없을 시 로그인 페이지 리다이렉트")
    void viewMyPointHistory_NoToken_Redirect() throws Exception {
        mockMvc.perform(get("/user/me/points"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("API: 현재 포인트 조회 성공")
    void getCurrentPoint_Api_Success() throws Exception {
        given(pointUserClient.getMyCurrentPoint(anyString())).willReturn(new CurrentPointResponseDto(5000));

        mockMvc.perform(get("/user/me/points/api/current")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPoint").value(5000));
    }

    @Test
    @DisplayName("API: 타입별 포인트 내역 조회 성공")
    void getPointHistory_Api_ByType() throws Exception {
        given(pointUserClient.getMyPointHistoryByType(anyString(), anyString(), anyInt(), anyInt()))
                .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/user/me/points/api/history")
                        .param("type", "EARN")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("API: 포인트 내역 조회 실패 시 502 에러 반환")
    void getPointHistory_Api_Fail_BadGateway() throws Exception {
        given(pointUserClient.getMyPointHistory(anyString(), anyInt(), anyInt()))
                .willThrow(new RuntimeException());

        mockMvc.perform(get("/user/me/points/api/history")
                        .cookie(authCookie))
                .andExpect(status().isBadGateway())
                .andExpect(content().string("포인트 내역을 불러오지 못했습니다."));
    }
}