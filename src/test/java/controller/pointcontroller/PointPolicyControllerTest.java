package controller.pointcontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.nhnacademy.book2onandonfrontservice.Book2OnAndOnFrontServiceApplication;
import com.nhnacademy.book2onandonfrontservice.client.PointPolicyAdminClient;
import com.nhnacademy.book2onandonfrontservice.controller.GlobalControllerAdvice;
import com.nhnacademy.book2onandonfrontservice.controller.pointController.PointPolicyController;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointPolicy.PointPolicyActiveUpdateRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointPolicy.PointPolicyResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointPolicy.PointPolicyUpdateRequestDto;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = PointPolicyController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = GlobalControllerAdvice.class
    )
)
@ContextConfiguration(classes = Book2OnAndOnFrontServiceApplication.class)
@WithMockUser(roles = "ADMIN")
class PointPolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AdminInterceptor adminInterceptor;
    @MockitoBean private PointPolicyAdminClient pointPolicyAdminClient;

    private final Cookie authCookie = new Cookie("accessToken", "test-token");

    @BeforeEach
    void setUp() throws Exception {
        given(adminInterceptor.preHandle(any(), any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("포인트 정책 전체 목록 조회 성공")
    void listPolicies_Success() throws Exception {
        PointPolicyResponseDto mockPolicy = mock(PointPolicyResponseDto.class);

        given(mockPolicy.getPointPolicyName()).willReturn("REGISTER");

        given(pointPolicyAdminClient.getAllPolicies(anyString())).willReturn(List.of(mockPolicy));

        mockMvc.perform(get("/admin/point-policies")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/point-policy"))
                .andExpect(model().attributeExists("policies"));
    }

    @Test
    @DisplayName("특정 포인트 정책 상세 조회 성공")
    void viewPolicy_Success() throws Exception {
        // [수정] 생성자 대신 mock()을 사용
        PointPolicyResponseDto mockPolicy = mock(PointPolicyResponseDto.class);

        given(pointPolicyAdminClient.getPolicy(anyString(), anyString())).willReturn(mockPolicy);

        mockMvc.perform(get("/admin/point-policies/REGISTER")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/point-policy"))
                .andExpect(model().attributeExists("policy"));
    }

    @Test
    @DisplayName("포인트 정책 수정 처리 및 상세 페이지 리다이렉트")
    void updatePolicy_Process() throws Exception {

        given(pointPolicyAdminClient.updatePolicy(anyInt(), any(PointPolicyUpdateRequestDto.class), anyString()))
                .willReturn(null);

        mockMvc.perform(post("/admin/point-policies/1")
                        .param("pointPolicyValue", "500")
                        .cookie(authCookie)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/point-policies/1"));
    }

    @Test
    @DisplayName("포인트 정책 활성화 상태 변경 처리 및 리다이렉트")
    void updatePolicyActive_Process() throws Exception {
        given(pointPolicyAdminClient.updatePolicyActive(anyInt(), any(PointPolicyActiveUpdateRequestDto.class), anyString()))
                .willReturn(null);

        mockMvc.perform(post("/admin/point-policies/1/active")
                        .param("isActive", "true")
                        .cookie(authCookie)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/point-policies/1"));
    }
}