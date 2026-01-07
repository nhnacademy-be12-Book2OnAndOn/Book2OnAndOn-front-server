package client.refund;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.nhnacademy.book2onandonfrontservice.client.RefundClient;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundAvailableItemResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundResponseDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = com.nhnacademy.book2onandonfrontservice.Book2OnAndOnFrontServiceApplication.class)
@ActiveProfiles("test")
class RefundClientTest {

    @MockBean
    private RefundClient refundClient;

    @Test
    @DisplayName("반품 신청 폼 조회")
    void getRefundForm_Success() {
        Long orderId = 1L;
        List<RefundAvailableItemResponseDto> response = List.of(mock(RefundAvailableItemResponseDto.class));

        given(refundClient.getRefundForm(any(), any(), eq(orderId))).willReturn(response);

        List<RefundAvailableItemResponseDto> result = refundClient.getRefundForm("token", null, orderId);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("반품 신청 처리")
    void createRefund_Success() {
        Long orderId = 1L;
        RefundRequestDto request = mock(RefundRequestDto.class);
        RefundResponseDto response = mock(RefundResponseDto.class);

        given(refundClient.createRefund(any(), any(), eq(orderId), any())).willReturn(response);

        RefundResponseDto result = refundClient.createRefund("token", null, orderId, request);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("반품 신청 취소 처리")
    void cancelRefund_Success() {
        Long orderId = 1L;
        Long refundId = 100L;
        RefundResponseDto response = mock(RefundResponseDto.class);

        given(refundClient.cancelRefund(any(), any(), eq(orderId), eq(refundId))).willReturn(response);

        RefundResponseDto result = refundClient.cancelRefund(null, "guestToken", orderId, refundId);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("반품 상세 정보 조회")
    void getRefundDetails_Success() {
        Long orderId = 1L;
        Long refundId = 100L;
        RefundResponseDto response = mock(RefundResponseDto.class);

        given(refundClient.getRefundDetails(any(), any(), eq(orderId), eq(refundId))).willReturn(response);

        RefundResponseDto result = refundClient.getRefundDetails("token", null, orderId, refundId);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("회원 본인의 반품 목록 조회")
    void getMyRefunds_Success() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<RefundResponseDto> response = new PageImpl<>(List.of());

        given(refundClient.getMyRefunds(any(), eq(pageable))).willReturn(response);

        Page<RefundResponseDto> result = refundClient.getMyRefunds("token", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("비정상적인 주문 번호로 반품 조회 시 예외 발생")
    void getRefundDetails_Fail_NotFound() {
        Long invalidOrderId = 999L;

        given(refundClient.getRefundDetails(any(), any(), eq(invalidOrderId), any()))
                .willThrow(feign.FeignException.NotFound.class);

        assertThatThrownBy(() -> refundClient.getRefundDetails(null, null, invalidOrderId, 1L))
                .isInstanceOf(Exception.class);
    }
}