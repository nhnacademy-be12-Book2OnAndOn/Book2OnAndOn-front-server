package controller.couponcontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nhnacademy.book2onandonfrontservice.Book2OnAndOnFrontServiceApplication;
import com.nhnacademy.book2onandonfrontservice.client.CouponClient;
import com.nhnacademy.book2onandonfrontservice.controller.GlobalControllerAdvice;
import com.nhnacademy.book2onandonfrontservice.controller.couponController.CouponController;
import com.nhnacademy.book2onandonfrontservice.dto.couponDto.CouponDto;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = CouponController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = GlobalControllerAdvice.class
    )
)
@ContextConfiguration(classes = Book2OnAndOnFrontServiceApplication.class)
@WithMockUser
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AdminInterceptor adminInterceptor;
    @MockitoBean private CouponClient couponClient;

    private final Cookie authCookie = new Cookie("accessToken", "test-token");

    @BeforeEach
    void setUp() throws Exception {
        given(adminInterceptor.preHandle(any(), any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("도서 및 카테고리에 적용 가능한 쿠폰 목록 조회 성공")
    void getAppliableCoupons_Success() throws Exception {
        CouponDto coupon = new CouponDto();
        given(couponClient.getAppliableCoupons(anyString(), anyLong(), anyList()))
                .willReturn(List.of(coupon));

        mockMvc.perform(get("/api/coupons/issuable")
                        .param("bookId", "1")
                        .param("categoryIds", "1,2,3")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("로그인하지 않은 사용자가 쿠폰 발급 시 401 응답")
    void issueCoupon_NoToken_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/coupons/1/issue")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("쿠폰 발급 요청 성공")
    void issueCoupon_Success() throws Exception {
        mockMvc.perform(post("/api/coupons/1/issue")
                        .cookie(authCookie)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("쿠폰이 발급되었습니다."));
    }

    @Test
    @DisplayName("이미 발급된 쿠폰인 경우 백엔드 메시지와 함께 에러 응답")
    void issueCoupon_FeignConflict_ReturnServerMessage() throws Exception {
        feign.FeignException.Conflict conflictException = mock(feign.FeignException.Conflict.class);
        given(conflictException.status()).willReturn(409);
        given(conflictException.contentUTF8()).willReturn("이미 발급된 쿠폰입니다.");

        org.mockito.Mockito.doThrow(conflictException)
                .when(couponClient).issueCoupon(anyString(), anyLong());

        mockMvc.perform(post("/api/coupons/1/issue")
                        .cookie(authCookie)
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(content().string("이미 발급된 쿠폰입니다."));
    }

    @Test
    @DisplayName("쿠폰 발급 중 시스템 오류 발생 시 500 응답")
    void issueCoupon_SystemError_InternalServerError() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("System Error"))
                .when(couponClient).issueCoupon(anyString(), anyLong());

        mockMvc.perform(post("/api/coupons/1/issue")
                        .cookie(authCookie)
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("시스템 오류가 발생했습니다."));
    }
}