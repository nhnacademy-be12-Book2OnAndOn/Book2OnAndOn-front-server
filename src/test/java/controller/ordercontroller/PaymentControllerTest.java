package controller.ordercontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.nhnacademy.book2onandonfrontservice.client.PaymentClient;
import com.nhnacademy.book2onandonfrontservice.controller.orderController.PaymentController;
import com.nhnacademy.book2onandonfrontservice.dto.paymentDto.request.CommonConfirmRequest;
import com.nhnacademy.book2onandonfrontservice.dto.paymentDto.response.PaymentResponse;
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
class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentClient paymentClient;

    @InjectMocks
    private PaymentController paymentController;

    private final String PROVIDER = "toss";
    private final String ORDER_ID = "order-123";
    private final String PAYMENT_KEY = "pay-key";
    private final Integer AMOUNT = 15000;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
    }

    @Test
    @DisplayName("결제 승인 성공 - 회원 (주문 완료 페이지로 리다이렉트)")
    void confirmPayment_Member_Success() throws Exception {
        PaymentResponse response = mock(PaymentResponse.class);
        given(response.orderNumber()).willReturn("ORD-001");
        given(paymentClient.confirmPayment(eq(PROVIDER), any(CommonConfirmRequest.class)))
                .willReturn(response);

        mockMvc.perform(get("/payment/" + PROVIDER + "/confirm")
                        .cookie(new Cookie("accessToken", "valid-token"))
                        .param("orderId", ORDER_ID)
                        .param("paymentKey", PAYMENT_KEY)
                        .param("amount", String.valueOf(AMOUNT)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/complete/ORD-001"));
    }

    @Test
    @DisplayName("결제 승인 성공 - 비회원 (비회원 전용 완료 뷰 반환)")
    void confirmPayment_Guest_Success() throws Exception {
        PaymentResponse response = mock(PaymentResponse.class);
        given(paymentClient.confirmPayment(eq(PROVIDER), any(CommonConfirmRequest.class)))
                .willReturn(response);

        mockMvc.perform(get("/payment/" + PROVIDER + "/confirm")
                        .param("orderId", ORDER_ID)
                        .param("paymentKey", PAYMENT_KEY)
                        .param("amount", String.valueOf(AMOUNT)))
                .andExpect(status().isOk())
                .andExpect(view().name("orderpayment/guest-order-complete"))
                .andExpect(model().attributeExists("payment"));
    }

    @Test
    @DisplayName("결제 승인 중 예외 발생 시 홈으로 리다이렉트")
    void confirmPayment_Exception_RedirectHome() throws Exception {
        given(paymentClient.confirmPayment(anyString(), any()))
                .willThrow(new RuntimeException("Payment Error"));

        mockMvc.perform(get("/payment/" + PROVIDER + "/confirm")
                        .param("orderId", ORDER_ID)
                        .param("paymentKey", PAYMENT_KEY)
                        .param("amount", String.valueOf(AMOUNT)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("결제 실패 콜백 처리")
    void failPayment_Success() throws Exception {
        mockMvc.perform(get("/payment/" + PROVIDER + "/fail")
                        .param("code", "PAY_PROCESS_CANCELED")
                        .param("message", "사용자가 취소함"))
                .andExpect(status().isOk())
                .andExpect(view().name("orderpayment/error"))
                .andExpect(model().attribute("provider", PROVIDER))
                .andExpect(model().attribute("code", "PAY_PROCESS_CANCELED"));
    }

    @Test
    @DisplayName("결제 자원 롤백 요청")
    void rollbackResources_Success() throws Exception {
        doNothing().when(paymentClient).rollbackResources(anyString());

        mockMvc.perform(get("/payment/ORD-001/rollback"))
                .andExpect(status().isNoContent());
    }
}