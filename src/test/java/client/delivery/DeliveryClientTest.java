package client.delivery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.nhnacademy.book2onandonfrontservice.client.DeliveryClient;
import com.nhnacademy.book2onandonfrontservice.dto.deliveryDto.DeliveryDto;
import com.nhnacademy.book2onandonfrontservice.dto.deliveryDto.DeliveryWaybillUpdateDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.status.OrderStatus;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

@ExtendWith(MockitoExtension.class)
class DeliveryClientTest {

    @Mock
    private DeliveryClient deliveryClient;

    private final String TOKEN = "Bearer test-token";
    private final String GUEST_TOKEN = "guest-token-123";

    @Test
    @DisplayName("주문 번호로 배송 정보 조회 성공")
    void getDeliveryByOrder_Success() {
        DeliveryDto expectedDto = mock(DeliveryDto.class);
        given(deliveryClient.getDeliveryByOrder(anyLong(), anyString(), anyString()))
                .willReturn(expectedDto);

        DeliveryDto result = deliveryClient.getDeliveryByOrder(1L, TOKEN, GUEST_TOKEN);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("관리자 배송 목록 페이징 조회 성공")
    void getDeliveries_Success() {
        @SuppressWarnings("unchecked")
        Page<DeliveryDto> expectedPage = mock(Page.class);
        given(deliveryClient.getDeliveries(eq(TOKEN), anyInt(), anyInt(), any(OrderStatus.class)))
                .willReturn(expectedPage);

        Page<DeliveryDto> result = deliveryClient.getDeliveries(TOKEN, 0, 10, OrderStatus.SHIPPING);

        assertThat(result).isEqualTo(expectedPage);
    }

    @Test
    @DisplayName("운송장 등록 성공")
    void registerWaybill_Success() {
        DeliveryWaybillUpdateDto requestDto = mock(DeliveryWaybillUpdateDto.class);
        deliveryClient.registerWaybill(TOKEN, 1L, requestDto);
        verify(deliveryClient).registerWaybill(TOKEN, 1L, requestDto);
    }

    @Test
    @DisplayName("배송 정보 수정 성공")
    void updateDeliveryInfo_Success() {
        DeliveryWaybillUpdateDto requestDto = mock(DeliveryWaybillUpdateDto.class);
        deliveryClient.updateDeliveryInfo(TOKEN, 1L, requestDto);
        verify(deliveryClient).updateDeliveryInfo(TOKEN, 1L, requestDto);
    }

    @Test
    @DisplayName("배송 정보 조회 시 데이터가 없으면 예외 발생")
    void getDeliveryByOrder_NotFound() {
        given(deliveryClient.getDeliveryByOrder(anyLong(), any(), any()))
                .willThrow(FeignException.NotFound.class);

        assertThatThrownBy(() -> deliveryClient.getDeliveryByOrder(999L, null, null))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("목록 조회 시 인증 실패 시 예외 발생")
    void getDeliveries_Unauthorized() {
        given(deliveryClient.getDeliveries(anyString(), anyInt(), anyInt(), any()))
                .willThrow(FeignException.Unauthorized.class);

        assertThatThrownBy(() -> deliveryClient.getDeliveries("invalid", 0, 10, null))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("운송장 등록 중 서버 에러 발생 시 예외 발생")
    void registerWaybill_ServerError() {
        doThrow(FeignException.InternalServerError.class)
                .when(deliveryClient).registerWaybill(anyString(), anyLong(), any(DeliveryWaybillUpdateDto.class));

        assertThatThrownBy(() -> deliveryClient.registerWaybill(TOKEN, 1L, new DeliveryWaybillUpdateDto()))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("배송 정보 수정 권한 없을 때 예외 발생")
    void updateDeliveryInfo_Forbidden() {
        doThrow(FeignException.Forbidden.class)
                .when(deliveryClient).updateDeliveryInfo(anyString(), anyLong(), any(DeliveryWaybillUpdateDto.class));

        assertThatThrownBy(() -> deliveryClient.updateDeliveryInfo(TOKEN, 1L, new DeliveryWaybillUpdateDto()))
                .isInstanceOf(FeignException.class);
    }
}