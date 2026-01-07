package client.orderpayment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import com.nhnacademy.book2onandonfrontservice.client.OrderUserClient;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderCreateRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderPrepareRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderCreateResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderDetailResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderPrepareResponseDto;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = com.nhnacademy.book2onandonfrontservice.Book2OnAndOnFrontServiceApplication.class)
@ActiveProfiles("test")
class OrderUserClientTest {

    @MockBean
    private OrderUserClient orderUserClient;

    @Test
    @DisplayName("주문서 준비 데이터 요청")
    void getOrderPrepare_Success() {
        OrderPrepareRequestDto request = new OrderPrepareRequestDto(List.of());
        OrderPrepareResponseDto response = mock(OrderPrepareResponseDto.class);

        given(orderUserClient.getOrderPrepare(any(), any())).willReturn(response);

        OrderPrepareResponseDto result = orderUserClient.getOrderPrepare("Bearer token", request);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("사전 주문 생성 요청")
    void createPreOrder_Success() {
        OrderCreateRequestDto request = mock(OrderCreateRequestDto.class);
        OrderCreateResponseDto response = mock(OrderCreateResponseDto.class);

        given(orderUserClient.createPreOrder(any(), any())).willReturn(response);

        OrderCreateResponseDto result = orderUserClient.createPreOrder("Bearer token", request);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("내 주문 목록 조회")
    void getOrderList_Success() {
        PageRequest pageable = PageRequest.of(0, 10);
        Map<String, Object> response = Map.of("content", List.of());

        given(orderUserClient.getOrderList(any(), eq(pageable))).willReturn(response);

        Map<String, Object> result = orderUserClient.getOrderList("Bearer token", pageable);

        assertThat(result).containsKey("content");
    }

    @Test
    @DisplayName("주문 상세 조회")
    void getOrderDetail_Success() {
        String orderNumber = "20240106-12345";
        OrderDetailResponseDto response = mock(OrderDetailResponseDto.class);

        given(orderUserClient.getOrderDetail(any(), any(), eq(orderNumber))).willReturn(response);

        OrderDetailResponseDto result = orderUserClient.getOrderDetail("Bearer token", null, orderNumber);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("주문 취소 처리")
    void cancelOrder_Success() {
        String orderNumber = "20240106-12345";

        doNothing().when(orderUserClient).cancelOrder(any(), any(), eq(orderNumber));

        orderUserClient.cancelOrder("Bearer token", null, orderNumber);
    }

    @Test
    @DisplayName("주문 상세 조회 시 권한 없음 예외 발생")
    void getOrderDetail_Fail_Unauthorized() {
        String orderNumber = "20240106-12345";

        given(orderUserClient.getOrderDetail(any(), any(), eq(orderNumber)))
                .willThrow(feign.FeignException.Unauthorized.class);

        assertThatThrownBy(() -> orderUserClient.getOrderDetail(null, null, orderNumber))
                .isInstanceOf(Exception.class);
    }
}