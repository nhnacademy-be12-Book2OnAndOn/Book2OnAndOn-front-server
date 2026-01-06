package controller.usercontroller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.nhnacademy.book2onandonfrontservice.client.*;
import com.nhnacademy.book2onandonfrontservice.controller.userController.MyPageViewController;
import com.nhnacademy.book2onandonfrontservice.dto.memberCouponDto.MemberCouponDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.CurrentPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointHistoryResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.RestPage;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.UserUpdateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.UserAddressResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.UserResponseDto;
import com.nhnacademy.book2onandonfrontservice.util.JwtUtils;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

class MyPageViewControllerTest {

    private MockMvc mockMvc;

    @Mock private UserClient userClient;
    @Mock private BookClient bookClient;
    @Mock private MemberCouponClient memberCouponClient;
    @Mock private OrderUserClient orderUserClient;
    @Mock private PointUserClient pointUserClient;
    @Mock private UserGradeClient userGradeClient;

    @InjectMocks private MyPageViewController myPageViewController;

    private final Cookie authCookie = new Cookie("accessToken", "valid-token");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(myPageViewController)
                .setViewResolvers(viewResolver)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("마이페이지 홈 - 정상 데이터 로드 확인")
    void myPageHome_Success() throws Exception {
        try (MockedStatic<JwtUtils> jwtUtils = mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.getUserId(anyString())).thenReturn(1L);

            UserResponseDto myInfo = mock(UserResponseDto.class);
            given(userClient.getMyInfo(anyString())).willReturn(myInfo);

            // [수정] 적절한 생성자를 사용하도록 변경 (List, Pageable, total)
            given(userClient.getUserReviews(anyLong(), anyInt(), anyInt()))
                    .willReturn(new RestPage<com.nhnacademy.book2onandonfrontservice.dto.userDto.response.BookReviewResponseDto>(
                            List.of(),           // content
                            PageRequest.of(0, 3), // pageable
                            0L                  // totalElements
                    ));

            given(memberCouponClient.getMyCoupon(anyString(), anyInt(), anyInt(), any())).willReturn(new PageImpl<>(List.of()));

            // orderUserClient가 Map을 반환하는 경우
            given(orderUserClient.getOrderList(anyString(), any())).willReturn(Map.of("content", List.of(), "totalElements", 0));

            given(userClient.getMyAddresses(anyString())).willReturn(List.of());
            given(pointUserClient.getMyPointHistory(anyString(), anyInt(), anyInt())).willReturn(new PageImpl<>(List.of()));
            given(pointUserClient.getMyCurrentPoint(anyString())).willReturn(new CurrentPointResponseDto(1000));

            mockMvc.perform(get("/users/me").cookie(authCookie))
                    .andExpect(status().isOk())
                    .andExpect(view().name("user/mypage/index"))
                    .andExpect(model().attributeExists("user", "currentPoint", "orderStatusSummary"));
        }
    }

    @Test
    @DisplayName("주소 목록 조회 성공")
    void addressList_Success() throws Exception {
        UserAddressResponseDto addr = mock(UserAddressResponseDto.class);
        given(userClient.getMyAddresses(anyString())).willReturn(List.of(addr));

        mockMvc.perform(get("/users/me/addresses").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("user/mypage/address-list"))
                .andExpect(model().attributeExists("addresses"));
    }

    @Test
    @DisplayName("내 정보 수정 처리 성공 - 리다이렉트 확인")
    void updateInfo_Success() throws Exception {
        // [수정] given() 대신 doNothing() 문법 사용
        doNothing().when(userClient).updateMyInfo(anyString(), any(com.nhnacademy.book2onandonfrontservice.dto.userDto.request.UserUpdateRequest.class));

        mockMvc.perform(post("/users/me/edit")
                        .cookie(authCookie)
                        .param("name", "수정된이름")
                        .param("nickname", "새닉네임")
                        .with(csrf())) // Standalone 설정 시 CSRF 무시 설정이 없다면 필요
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/me?success=update"));
    }

    @Test
    @DisplayName("회원 탈퇴 처리 - 쿠키 만료 및 메인 리다이렉트")
    void withdraw_Success() throws Exception {
        doNothing().when(userClient).withdrawUser(anyString(), anyString());

        mockMvc.perform(post("/users/me/withdraw")
                        .cookie(authCookie)
                        .param("reason", "개인정보보호"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?message=withdrawn"))
                .andExpect(cookie().maxAge("accessToken", 0));
    }

    @Test
    @DisplayName("좋아요 도서 목록 조회")
    void myLikes_Success() throws Exception {
        try (MockedStatic<JwtUtils> jwtUtils = mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.getUserId(anyString())).thenReturn(1L);
            
            given(userClient.getMyInfo(anyString())).willReturn(mock(UserResponseDto.class));
            given(userClient.getMyLikedBooks(anyString(), anyLong(), anyInt(), anyInt()))
                    .willReturn(new PageImpl<>(List.of()));

            mockMvc.perform(get("/users/me/likes").cookie(authCookie))
                    .andExpect(status().isOk())
                    .andExpect(view().name("user/mypage/likes"))
                    .andExpect(model().attributeExists("books"));
        }
    }
}