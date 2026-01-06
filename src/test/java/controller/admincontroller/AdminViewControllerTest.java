package com.nhnacademy.book2onandonfrontservice.controller.adminController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.nhnacademy.book2onandonfrontservice.Book2OnAndOnFrontServiceApplication;
import com.nhnacademy.book2onandonfrontservice.client.*;
import com.nhnacademy.book2onandonfrontservice.dto.deliveryDto.DeliveryPolicyDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.CurrentPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.RestPage;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.UserGradeDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.UserResponseDto;
import com.nhnacademy.book2onandonfrontservice.interceptor.AdminInterceptor;
import jakarta.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminViewController.class)
@ContextConfiguration(classes = Book2OnAndOnFrontServiceApplication.class)
@WithMockUser(roles = "ADMIN")
class AdminViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AdminInterceptor adminInterceptor;

    @MockitoBean private UserClient userClient;
    @MockitoBean private CouponClient couponClient;
    @MockitoBean private BookClient bookClient;
    @MockitoBean private BookReindexClient bookReindexClient;
    @MockitoBean private UserGradeClient userGradeClient;
    @MockitoBean private DeliveryClient deliveryClient;
    @MockitoBean private DeliveryPolicyClient deliveryPolicyClient;
    @MockitoBean private PointAdminClient pointAdminClient;
    @MockitoBean private OrderUserClient orderUserClient;

    private final Cookie authCookie = new Cookie("accessToken", "test-token");

    @BeforeEach
    void setUp() throws Exception {
        given(adminInterceptor.preHandle(any(), any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("관리자 대시보드 조회 성공")
    void dashboard_Success() throws Exception {
        given(userClient.getUserCount(anyString())).willReturn(ResponseEntity.ok(100L));
        given(bookClient.countAllBook(anyString())).willReturn(500L);

        mockMvc.perform(get("/admin").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/index"))
                .andExpect(model().attributeExists("totalUserCount", "bookCount"));
    }

    @Test
    @DisplayName("대시보드 조회 시 API 예외 발생해도 기본값 0으로 노출")
    void dashboard_ApiFail_ShowZero() throws Exception {
        given(userClient.getUserCount(anyString())).willThrow(new RuntimeException());
        given(bookClient.countAllBook(anyString())).willThrow(new RuntimeException());

        mockMvc.perform(get("/admin").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(model().attribute("totalUserCount", 0))
                .andExpect(model().attribute("bookCount", 0));
    }

    @Test
    @DisplayName("회원 목록 조회 성공")
    void userList_Success() throws Exception {
        List<UserResponseDto> content = new ArrayList<>();
        PageRequest pageable = PageRequest.of(0, 10);
        RestPage<UserResponseDto> userPage = new RestPage<>(content, pageable, 0L);

        given(userClient.getUsers(anyString(), anyInt(), anyInt())).willReturn(userPage);

        mockMvc.perform(get("/admin/users").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attributeExists("users", "page"));
    }

    @Test
    @DisplayName("회원 상세 조회 성공")
    void userDetail_Success() throws Exception {
        UserResponseDto userDto = new UserResponseDto();
        CurrentPointResponseDto pointDto = mock(CurrentPointResponseDto.class);
        given(userClient.getUserDetail(anyString(), anyLong())).willReturn(userDto);
        given(pointAdminClient.getUserCurrentPoint(anyString(), anyLong())).willReturn(pointDto);

        mockMvc.perform(get("/admin/users/1").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-detail"))
                .andExpect(model().attributeExists("targetUser", "currentPoint"));
    }

    @Test
    @DisplayName("회원 정보 수정 성공")
    void updateUser_Success() throws Exception {
        mockMvc.perform(post("/admin/users/1/update")
                        .with(csrf())
                        .cookie(authCookie)
                        .param("name", "수정"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users?success=update"));
    }

    @Test
    @DisplayName("회원 강제 탈퇴 성공")
    void deleteUser_Success() throws Exception {
        mockMvc.perform(post("/admin/users/1/delete")
                        .with(csrf())
                        .cookie(authCookie)
                        .param("reason", "규정 위반"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users?success=delete"));
    }

    @Test
    @DisplayName("쿠폰 목록 조회 성공")
    void listCoupons_Success() throws Exception {
        PageRequest pageable = PageRequest.of(0, 10);
        given(couponClient.getCoupons(anyString(), anyInt(), anyInt(), any()))
                .willReturn(new PageImpl<>(List.of(), pageable, 0L));

        mockMvc.perform(get("/admin/coupons").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/coupon/list"));
    }

    @Test
    @DisplayName("배송 목록 조회 성공")
    void listDeliveries_Success() throws Exception {
        PageRequest pageable = PageRequest.of(0, 10);
        given(deliveryClient.getDeliveries(anyString(), anyInt(), anyInt(), any()))
                .willReturn(new PageImpl<>(List.of(), pageable, 0L));

        mockMvc.perform(get("/admin/deliveries").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/delivery/list"));
    }

    @Test
    @DisplayName("배송 정책 목록 조회 성공")
    void getDeliveryPolicies_Success() throws Exception {
        PageRequest pageable = PageRequest.of(0, 10);
        given(deliveryPolicyClient.getDeliveryPolicies(anyString(), anyInt(), anyInt()))
                .willReturn(new PageImpl<>(List.of(), pageable, 0L));

        mockMvc.perform(get("/admin/delivery-policies").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/delivery/policy-list"));
    }

    @Test
    @DisplayName("배송 정책 수정 폼 조회 성공")
    void updateForm_Success() throws Exception {
        DeliveryPolicyDto policy = new DeliveryPolicyDto();
        policy.setDeliveryPolicyId(1L);
        PageRequest pageable = PageRequest.of(0, 10);
        given(deliveryPolicyClient.getDeliveryPolicies(anyString(), anyInt(), anyInt()))
                .willReturn(new PageImpl<>(List.of(policy), pageable, 1L));

        mockMvc.perform(get("/admin/delivery-policies/update/1").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/delivery/form"));
    }

    @Test
    @DisplayName("포인트 관리 페이지 조회 성공")
    void listUserPointHistory_Success() throws Exception {
        PageRequest pageable = PageRequest.of(0, 10);
        given(pointAdminClient.getUserPointHistory(anyString(), anyLong(), anyInt(), anyInt()))
                .willReturn(new PageImpl<>(List.of(), pageable, 0L));
        given(pointAdminClient.getUserCurrentPoint(anyString(), anyLong()))
                .willReturn(mock(CurrentPointResponseDto.class));

        mockMvc.perform(get("/admin/points").cookie(authCookie).param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/point-history-admin"));
    }

    @Test
    @DisplayName("포인트 조정 성공")
    void adjustUserPoint_Success() throws Exception {
        mockMvc.perform(post("/admin/points/adjust")
                        .with(csrf())
                        .cookie(authCookie)
                        .param("userId", "1")
                        .param("amount", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/points?userId=1"));
    }

    @Test
    @DisplayName("등급 목록 조회 성공")
    void gradeList_Success() throws Exception {
        given(userGradeClient.getAllGrades()).willReturn(List.of(new UserGradeDto()));

        mockMvc.perform(get("/admin/grades").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/grades/list"));
    }

    @Test
    @DisplayName("인증 쿠키 없을 시 로그인 페이지 리다이렉트")
    void auth_NoToken_Redirect() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
