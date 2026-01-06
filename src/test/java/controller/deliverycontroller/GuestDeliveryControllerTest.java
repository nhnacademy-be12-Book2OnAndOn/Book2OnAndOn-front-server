package controller.deliverycontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.nhnacademy.book2onandonfrontservice.Book2OnAndOnFrontServiceApplication;
import com.nhnacademy.book2onandonfrontservice.client.DeliveryClient;
import com.nhnacademy.book2onandonfrontservice.controller.GlobalControllerAdvice;
import com.nhnacademy.book2onandonfrontservice.controller.deliveryController.GuestDeliveryController;
import com.nhnacademy.book2onandonfrontservice.dto.deliveryDto.DeliveryDto;
import com.nhnacademy.book2onandonfrontservice.interceptor.AdminInterceptor;
import jakarta.servlet.http.Cookie;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = GuestDeliveryController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = GlobalControllerAdvice.class
    )
)
@ContextConfiguration(classes = Book2OnAndOnFrontServiceApplication.class)
@WithMockUser
class GuestDeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AdminInterceptor adminInterceptor;
    @MockitoBean private DeliveryClient deliveryClient;

    @BeforeEach
    void setUp() throws Exception {
        given(adminInterceptor.preHandle(any(), any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("비회원 배송 추적 성공 및 모델 값 확인")
    void trackDelivery_Success() throws Exception {
        DeliveryDto deliveryDto = new DeliveryDto();

        given(deliveryClient.getDeliveryByOrder(anyLong(), isNull(), any()))
                .willReturn(deliveryDto);

        mockMvc.perform(get("/guest/delivery")
                        .param("orderId", "100")
                        .cookie(new Cookie("guestOrderToken", "test-guest-token")))
                .andExpect(status().isOk())
                .andExpect(view().name("user/guest/deliveryTrack"))
                .andExpect(model().attributeExists("delivery"));
    }

    @Test
    @DisplayName("비회원 주문 토큰 쿠키가 없는 경우 로그인 페이지로 리다이렉트")
    void trackDelivery_NoToken_RedirectGuestLogin() throws Exception {
        mockMvc.perform(get("/guest/delivery")
                        .param("orderId", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/guest/login"));
    }
}
