package client.orderpayment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import com.nhnacademy.book2onandonfrontservice.client.PaymentClient;
import com.nhnacademy.book2onandonfrontservice.dto.paymentDto.request.CommonConfirmRequest;
import com.nhnacademy.book2onandonfrontservice.dto.paymentDto.response.PaymentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = com.nhnacademy.book2onandonfrontservice.Book2OnAndOnFrontServiceApplication.class)
@ActiveProfiles("test")
class PaymentClientTest {

    @MockBean
    private PaymentClient paymentClient;

    @Test
    @DisplayName("결제 승인 요청")
    void confirmPayment_Success() {
        String provider = "toss";
        CommonConfirmRequest request = mock(CommonConfirmRequest.class);
        PaymentResponse response = mock(PaymentResponse.class);

        given(paymentClient.confirmPayment(eq(provider), any(CommonConfirmRequest.class)))
                .willReturn(response);

        PaymentResponse result = paymentClient.confirmPayment(provider, request);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("결제 실패 시 자원 롤백 요청")
    void rollbackResources_Success() {
        String orderNumber = "20240106-ORDER-001";

        doNothing().when(paymentClient).rollbackResources(eq(orderNumber));

        paymentClient.rollbackResources(orderNumber);
    }

    @Test
    @DisplayName("외부 결제사 승인 거절 시 예외 발생")
    void confirmPayment_Fail_ProviderError() {
        String provider = "toss";
        CommonConfirmRequest request = mock(CommonConfirmRequest.class);

        given(paymentClient.confirmPayment(eq(provider), any()))
                .willThrow(feign.FeignException.BadRequest.class);

        assertThatThrownBy(() -> paymentClient.confirmPayment(provider, request))
                .isInstanceOf(Exception.class);
    }
}