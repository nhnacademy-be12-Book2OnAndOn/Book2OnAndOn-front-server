package controller.ordercontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.nhnacademy.book2onandonfrontservice.client.GuestOrderClient;
import com.nhnacademy.book2onandonfrontservice.client.OrderUserClient;
import com.nhnacademy.book2onandonfrontservice.controller.orderController.OrderGuestController;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderDetailResponseDto;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class OrderGuestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderUserClient orderUserClient;

    @Mock
    private GuestOrderClient guestOrderClient;

    @InjectMocks
    private OrderGuestController orderGuestController;

    private final String GUEST_TOKEN = "test-guest-token";
    private final String ORDER_NUMBER = "20240106-GUEST-001";

    @BeforeEach
    void setUp() {
        // 독립형 설정으로 보안 필터 및 GlobalAdvice의 간섭을 받지 않음
        mockMvc = MockMvcBuilders.standaloneSetup(orderGuestController).build();
    }

    @Test
    @DisplayName("비회원 주문 상세 조회 성공")
    void getOrderDetail_Success() throws Exception {
        OrderDetailResponseDto response = mock(OrderDetailResponseDto.class);
        
        // OrderUserClient.getOrderDetail(accessToken, guestToken, orderNumber)
        given(orderUserClient.getOrderDetail(isNull(), eq(GUEST_TOKEN), eq(ORDER_NUMBER)))
                .willReturn(response);

        mockMvc.perform(get("/guest/orders/" + ORDER_NUMBER)
                        .cookie(new Cookie("guestOrderToken", GUEST_TOKEN)))
                .andExpect(status().isOk())
                .andExpect(view().name("orderpayment/guest-order-detail"))
                .andExpect(model().attributeExists("order"));
    }

    @Test
    @DisplayName("쿠키가 없을 때 주문 상세 조회 시 로그인 페이지로 리다이렉트")
    void getOrderDetail_NoCookie_Redirect() throws Exception {
        mockMvc.perform(get("/guest/orders/" + ORDER_NUMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/guest/login"));
    }

    @Test
    @DisplayName("비회원 주문 취소 성공 및 상세 페이지로 리다이렉트")
    void cancelGuestOrder_Success() throws Exception {
        doNothing().when(guestOrderClient).cancelOrder(eq(ORDER_NUMBER), eq(GUEST_TOKEN));

        mockMvc.perform(get("/guest/orders/" + ORDER_NUMBER + "/cancel")
                        .cookie(new Cookie("guestOrderToken", GUEST_TOKEN)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/guest/orders/" + ORDER_NUMBER));
    }

    @Test
    @DisplayName("쿠키가 없을 때 주문 취소 시 로그인 페이지로 리다이렉트")
    void cancelGuestOrder_NoCookie_Redirect() throws Exception {
        mockMvc.perform(get("/guest/orders/" + ORDER_NUMBER + "/cancel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/guest/login"));
    }
}