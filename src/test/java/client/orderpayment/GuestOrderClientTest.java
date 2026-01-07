package client.orderpayment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import com.nhnacademy.book2onandonfrontservice.client.GuestOrderClient;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.GuestLoginRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.GuestLoginResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.guest.GuestOrderCreateRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderPrepareRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderCreateResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderPrepareResponseDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = com.nhnacademy.book2onandonfrontservice.Book2OnAndOnFrontServiceApplication.class)
@ActiveProfiles("test")
class GuestOrderClientTest {

    @MockBean
    private GuestOrderClient guestOrderClient;

    @Test
    @DisplayName("비회원 주문 로그인 요청")
    void loginGuest_Success() {
        GuestLoginRequestDto request = new GuestLoginRequestDto("orderNum", "pw");
        GuestLoginResponseDto response = new GuestLoginResponseDto("guest-id", "token");

        given(guestOrderClient.loginGuest(any())).willReturn(ResponseEntity.ok(response));

        ResponseEntity<GuestLoginResponseDto> result = guestOrderClient.loginGuest(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(result.getBody().getAccessToken()).isEqualTo("guest-id");
    }

    @Test
    @DisplayName("비회원 주문서 준비 데이터 요청")
    void getOrderPrepare_Success() {
        OrderPrepareRequestDto request = new OrderPrepareRequestDto(List.of());
        OrderPrepareResponseDto response = mock(OrderPrepareResponseDto.class);

        given(guestOrderClient.getOrderPrepare(eq("test-guest"), any())).willReturn(response);

        OrderPrepareResponseDto result = guestOrderClient.getOrderPrepare("test-guest", request);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("비회원 주문 생성")
    void createGuestOrder_Success() {
        GuestOrderCreateRequestDto request = mock(GuestOrderCreateRequestDto.class);
        OrderCreateResponseDto response = mock(OrderCreateResponseDto.class);

        given(guestOrderClient.createGuestOrder(eq("test-guest"), any())).willReturn(response);

        OrderCreateResponseDto result = guestOrderClient.createGuestOrder("test-guest", request);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("비회원 주문 취소 처리")
    void cancelOrder_Success() {
        String orderNumber = "20240101-001";
        String guestToken = "token-123";

        doNothing().when(guestOrderClient).cancelOrder(orderNumber, guestToken);

        guestOrderClient.cancelOrder(orderNumber, guestToken);
    }

    @Test
    @DisplayName("비회원 주문 로그인 실패")
    void loginGuest_Fail_Unauthorized() {
        GuestLoginRequestDto request = new GuestLoginRequestDto("wrong", "wrong");

        given(guestOrderClient.loginGuest(any()))
                .willThrow(feign.FeignException.Unauthorized.class);

        assertThatThrownBy(() -> guestOrderClient.loginGuest(request))
                .isInstanceOf(Exception.class);
    }
}