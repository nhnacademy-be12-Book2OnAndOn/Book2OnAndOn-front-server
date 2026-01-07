package client.refund;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.nhnacademy.book2onandonfrontservice.client.RefundAdminClient;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundSearchCondition;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundStatusUpdateRequestDto;
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
class RefundAdminClientTest {

    @MockBean
    private RefundAdminClient refundAdminClient;

    @Test
    @DisplayName("관리자 반품 목록 조회")
    void getRefundList_Success() {
        RefundSearchCondition condition = mock(RefundSearchCondition.class);
        PageRequest pageable = PageRequest.of(0, 10);
        Page<RefundResponseDto> response = new PageImpl<>(List.of());

        given(refundAdminClient.getRefundList(any(), eq(condition), eq(pageable))).willReturn(response);

        Page<RefundResponseDto> result = refundAdminClient.getRefundList("Bearer token", condition, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("관리자 반품 상세 정보 조회")
    void findRefundDetails_Success() {
        Long refundId = 1L;
        RefundResponseDto response = mock(RefundResponseDto.class);

        given(refundAdminClient.findRefundDetails(any(), eq(refundId))).willReturn(response);

        RefundResponseDto result = refundAdminClient.findRefundDetails("Bearer token", refundId);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("관리자 반품 상태 변경 처리")
    void updateRefundStatus_Success() {
        Long refundId = 1L;
        RefundStatusUpdateRequestDto requestDto = mock(RefundStatusUpdateRequestDto.class);
        RefundResponseDto response = mock(RefundResponseDto.class);

        given(refundAdminClient.updateRefundStatus(any(), eq(refundId), any())).willReturn(response);

        RefundResponseDto result = refundAdminClient.updateRefundStatus("Bearer token", refundId, requestDto);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 반품 상세 조회 시 예외 발생")
    void findRefundDetails_Fail_NotFound() {
        Long invalidRefundId = 999L;

        given(refundAdminClient.findRefundDetails(any(), eq(invalidRefundId)))
                .willThrow(feign.FeignException.NotFound.class);

        assertThatThrownBy(() -> refundAdminClient.findRefundDetails(null, invalidRefundId))
                .isInstanceOf(Exception.class);
    }
}