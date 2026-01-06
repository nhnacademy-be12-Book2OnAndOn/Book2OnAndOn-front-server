package controller.couponcontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.nhnacademy.book2onandonfrontservice.Book2OnAndOnFrontServiceApplication;
import com.nhnacademy.book2onandonfrontservice.client.MemberCouponClient;
import com.nhnacademy.book2onandonfrontservice.controller.GlobalControllerAdvice;
import com.nhnacademy.book2onandonfrontservice.controller.couponController.MemberCouponViewController;
import com.nhnacademy.book2onandonfrontservice.dto.memberCouponDto.MemberCouponDto;
import com.nhnacademy.book2onandonfrontservice.dto.memberCouponDto.MemberCouponStatus;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = MemberCouponViewController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = GlobalControllerAdvice.class
    )
)
@ContextConfiguration(classes = Book2OnAndOnFrontServiceApplication.class)
@WithMockUser
class MemberCouponViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AdminInterceptor adminInterceptor;
    @MockitoBean private MemberCouponClient memberCouponClient;

    private final Cookie authCookie = new Cookie("accessToken", "test-token");

    @BeforeEach
    void setUp() throws Exception {
        given(adminInterceptor.preHandle(any(), any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("내 쿠폰 목록 조회 성공 및 모델 값 확인")
    void myCouponList_Success() throws Exception {
        com.nhnacademy.book2onandonfrontservice.dto.memberCouponDto.MemberCouponDto couponDto =
                new com.nhnacademy.book2onandonfrontservice.dto.memberCouponDto.MemberCouponDto();

        org.springframework.test.util.ReflectionTestUtils.setField(
                couponDto,
                "memberCouponStatus",
                com.nhnacademy.book2onandonfrontservice.dto.memberCouponDto.MemberCouponStatus.NOT_USED
        );

        PageRequest pageable = PageRequest.of(0, 10);
        given(memberCouponClient.getMyCoupon(anyString(), anyInt(), anyInt(), any()))
                .willReturn(new PageImpl<>(List.of(couponDto), pageable, 1));

        String statusParam = com.nhnacademy.book2onandonfrontservice.dto.memberCouponDto.MemberCouponStatus.NOT_USED.name();

        mockMvc.perform(get("/users/me/coupons")
                        .cookie(authCookie)
                        .param("page", "0")
                        .param("status", statusParam))
                .andExpect(status().isOk())
                .andExpect(view().name("memberCoupon"))
                .andExpect(model().attributeExists("myCoupons"));
    }

    @Test
    @DisplayName("액세스 토큰 쿠키가 없는 경우 로그인 페이지로 리다이렉트")
    void myCouponList_NoToken_RedirectLogin() throws Exception {
        mockMvc.perform(get("/users/me/coupons"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("내 쿠폰 목록 조회 시 페이징 시작/끝 페이지 계산 로직 확인")
    void myCouponList_PaginationLogic() throws Exception {
        PageRequest pageable = PageRequest.of(5, 10);
        given(memberCouponClient.getMyCoupon(anyString(), anyInt(), anyInt(), any()))
                .willReturn(new PageImpl<>(List.of(), pageable, 100));

        mockMvc.perform(get("/users/me/coupons")
                        .cookie(authCookie)
                        .param("page", "5"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("startPage", 3))
                .andExpect(model().attribute("endPage", 7));
    }
}