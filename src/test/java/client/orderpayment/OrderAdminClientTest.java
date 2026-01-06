package client.orderpayment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import com.nhnacademy.book2onandonfrontservice.client.OrderAdminClient;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderItemStatusUpdateDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderStatusUpdateDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderDetailResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderSimpleDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.PageResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = com.nhnacademy.book2onandonfrontservice.Book2OnAndOnFrontServiceApplication.class)
@ActiveProfiles("test")
class OrderAdminClientTest {

    @MockBean
    private OrderAdminClient orderAdminClient;

    @Test
    @DisplayName("관리자 주문 목록 조회")
    void getOrderList_Success() {
        PageRequest pageable = PageRequest.of(0, 10);
        PageResponse<OrderSimpleDto> response = new PageResponse<>(
                List.of(), // content
                0,         // page
                10,        // size
                0,         // totalPages
                0L,        // totalElements
                true       // last
        );
        given(orderAdminClient.getOrderList(any())).willReturn(response);

        PageResponse<OrderSimpleDto> result = orderAdminClient.getOrderList(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("주문 상세 정보 조회")
    void getOrderDetail_Success() {
        String orderNumber = "20240106-0001";
        OrderDetailResponseDto response = mock(OrderDetailResponseDto.class);

        given(orderAdminClient.getOrderDetail(eq(orderNumber))).willReturn(response);

        OrderDetailResponseDto result = orderAdminClient.getOrderDetail(orderNumber);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("주문 상태 수정 처리")
    void updateOrderStatus_Success() {
        String orderNumber = "20240106-0001";
        OrderStatusUpdateDto request = mock(OrderStatusUpdateDto.class);

        doNothing().when(orderAdminClient).updateOrderStatus(eq(orderNumber), any());

        orderAdminClient.updateOrderStatus(orderNumber, request);
    }

    @Test
    @DisplayName("주문 아이템 상태 수정 처리")
    void updateOrderItemStatus_Success() {
        String orderNumber = "20240106-0001";
        OrderItemStatusUpdateDto request = mock(OrderItemStatusUpdateDto.class);

        doNothing().when(orderAdminClient).updateOrderItemStatus(eq(orderNumber), any());

        orderAdminClient.updateOrderItemStatus(orderNumber, request);
    }

    @Test
    @DisplayName("관리자 주문 취소 처리")
    void cancelOrder_Success() {
        String orderNumber = "20240106-0001";

        doNothing().when(orderAdminClient).cancelOrder(eq(orderNumber));

        orderAdminClient.cancelOrder(orderNumber);
    }

    @Test
    @DisplayName("존재하지 않는 주문 조회 시 예외 발생")
    void getOrderDetail_Fail_NotFound() {
        String invalidOrderNumber = "INVALID";

        given(orderAdminClient.getOrderDetail(invalidOrderNumber))
                .willThrow(feign.FeignException.NotFound.class);

        assertThatThrownBy(() -> orderAdminClient.getOrderDetail(invalidOrderNumber))
                .isInstanceOf(feign.FeignException.class);
    }
}