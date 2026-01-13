//package client.delivery;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyInt;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//
//import com.nhnacademy.book2onandonfrontservice.client.DeliveryPolicyClient;
//import com.nhnacademy.book2onandonfrontservice.dto.deliveryDto.DeliveryPolicyDto;
//import feign.FeignException;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//
//@ExtendWith(MockitoExtension.class)
//class DeliveryPolicyClientTest {
//
//    @Mock
//    private DeliveryPolicyClient deliveryPolicyClient;
//
//    private final String TOKEN = "Bearer test-token";
//
//    @Test
//    @DisplayName("관리자용 배송 정책 페이징 목록 조회 성공")
//    void getDeliveryPolicies_Success() {
//        @SuppressWarnings("unchecked")
//        Page<DeliveryPolicyDto> expectedPage = mock(Page.class);
//        given(deliveryPolicyClient.getDeliveryPolicies(eq(TOKEN), anyInt(), anyInt()))
//                .willReturn(expectedPage);
//
//        Page<DeliveryPolicyDto> result = deliveryPolicyClient.getDeliveryPolicies(TOKEN, 0, 10);
//
//        assertThat(result).isEqualTo(expectedPage);
//    }
//
//    @Test
//    @DisplayName("배송 정책 단건 상세 조회 성공")
//    void getDeliveryPolicy_WithId_Success() {
//        Long policyId = 1L;
//        DeliveryPolicyDto expectedDto = mock(DeliveryPolicyDto.class);
//        given(deliveryPolicyClient.getDeliveryPolicy(TOKEN, policyId)).willReturn(expectedDto);
//
//        DeliveryPolicyDto result = deliveryPolicyClient.getDeliveryPolicy(TOKEN, policyId);
//
//        assertThat(result).isEqualTo(expectedDto);
//    }
//
//    @Test
//    @DisplayName("배송 정책 생성 성공")
//    void createDeliveryPolicy_Success() {
//        DeliveryPolicyDto requestDto = mock(DeliveryPolicyDto.class);
//        deliveryPolicyClient.createDeliveryPolicy(TOKEN, requestDto);
//        verify(deliveryPolicyClient).createDeliveryPolicy(TOKEN, requestDto);
//    }
//
//    @Test
//    @DisplayName("배송 정책 수정 성공")
//    void updateDeliveryPolicy_Success() {
//        Long policyId = 1L;
//        DeliveryPolicyDto requestDto = mock(DeliveryPolicyDto.class);
//        deliveryPolicyClient.updateDeliveryPolicy(TOKEN, policyId, requestDto);
//        verify(deliveryPolicyClient).updateDeliveryPolicy(TOKEN, policyId, requestDto);
//    }
//
//    @Test
//    @DisplayName("사용자용 배송 정책 목록 조회 성공")
//    void getDeliveryPolicy_NoArgs_Success() {
//        @SuppressWarnings("unchecked")
//        Page<DeliveryPolicyDto> expectedPage = mock(Page.class);
//        given(deliveryPolicyClient.getDeliveryPolicy()).willReturn(expectedPage);
//
//        Page<DeliveryPolicyDto> result = deliveryPolicyClient.getDeliveryPolicy();
//
//        assertThat(result).isEqualTo(expectedPage);
//    }
//
//    @Test
//    @DisplayName("권한 없는 사용자가 정책 조회 시 예외 발생")
//    void getDeliveryPolicy_Unauthorized() {
//        given(deliveryPolicyClient.getDeliveryPolicy(anyString(), anyLong()))
//                .willThrow(FeignException.Unauthorized.class);
//
//        assertThatThrownBy(() -> deliveryPolicyClient.getDeliveryPolicy("invalid", 1L))
//                .isInstanceOf(FeignException.class);
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 정책 조회 시 예외 발생")
//    void getDeliveryPolicy_NotFound() {
//        given(deliveryPolicyClient.getDeliveryPolicy(anyString(), anyLong()))
//                .willThrow(FeignException.NotFound.class);
//
//        assertThatThrownBy(() -> deliveryPolicyClient.getDeliveryPolicy(TOKEN, 999L))
//                .isInstanceOf(FeignException.class);
//    }
//
//    @Test
//    @DisplayName("정책 생성 중 서버 오류 발생 시 예외 발생")
//    void createDeliveryPolicy_ServerError() {
//        doThrow(FeignException.InternalServerError.class)
//                .when(deliveryPolicyClient).createDeliveryPolicy(anyString(), any(DeliveryPolicyDto.class));
//
//        assertThatThrownBy(() -> deliveryPolicyClient.createDeliveryPolicy(TOKEN, new DeliveryPolicyDto()))
//                .isInstanceOf(FeignException.class);
//    }
//
//    @Test
//    @DisplayName("정책 수정 시 잘못된 요청으로 인한 예외 발생")
//    void updateDeliveryPolicy_BadRequest() {
//        doThrow(FeignException.BadRequest.class)
//                .when(deliveryPolicyClient).updateDeliveryPolicy(anyString(), anyLong(), any(DeliveryPolicyDto.class));
//
//        assertThatThrownBy(() -> deliveryPolicyClient.updateDeliveryPolicy(TOKEN, 1L, new DeliveryPolicyDto()))
//                .isInstanceOf(FeignException.class);
//    }
//}