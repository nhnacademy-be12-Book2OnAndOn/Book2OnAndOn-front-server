package client.point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.nhnacademy.book2onandonfrontservice.client.PointUserClient;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.*;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

@ExtendWith(MockitoExtension.class)
class PointUserClientTest {

    @Mock
    private PointUserClient pointUserClient;

    private final String TOKEN = "Bearer test-token";

    @Test
    @DisplayName("내 포인트 이력 페이징 조회 성공")
    void getMyPointHistory_Success() {
        @SuppressWarnings("unchecked")
        Page<PointHistoryResponseDto> expectedPage = mock(Page.class);
        given(pointUserClient.getMyPointHistory(eq(TOKEN), anyInt(), anyInt()))
                .willReturn(expectedPage);

        Page<PointHistoryResponseDto> result = pointUserClient.getMyPointHistory(TOKEN, 0, 10);

        assertThat(result).isEqualTo(expectedPage);
    }

    @Test
    @DisplayName("타입 필터 기반 포인트 이력 조회 성공")
    void getMyPointHistoryByType_Success() {
        @SuppressWarnings("unchecked")
        Page<PointHistoryResponseDto> expectedPage = mock(Page.class);
        given(pointUserClient.getMyPointHistoryByType(eq(TOKEN), anyString(), anyInt(), anyInt()))
                .willReturn(expectedPage);

        Page<PointHistoryResponseDto> result = pointUserClient.getMyPointHistoryByType(TOKEN, "EARN", 0, 10);

        assertThat(result).isEqualTo(expectedPage);
    }

    @Test
    @DisplayName("내 현재 포인트 조회 성공")
    void getMyCurrentPoint_Success() {
        CurrentPointResponseDto expectedDto = mock(CurrentPointResponseDto.class);
        given(pointUserClient.getMyCurrentPoint(TOKEN)).willReturn(expectedDto);

        CurrentPointResponseDto result = pointUserClient.getMyCurrentPoint(TOKEN);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("회원가입 적립 성공")
    void earnSignupPoint_Success() {
        EarnPointResponseDto expectedDto = mock(EarnPointResponseDto.class);
        given(pointUserClient.earnSignupPoint(TOKEN)).willReturn(expectedDto);

        EarnPointResponseDto result = pointUserClient.earnSignupPoint(TOKEN);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("리뷰 작성 적립 성공")
    void earnReviewPoint_Success() {
        EarnReviewPointRequestDto requestDto = mock(EarnReviewPointRequestDto.class);
        EarnPointResponseDto expectedDto = mock(EarnPointResponseDto.class);
        given(pointUserClient.earnReviewPoint(TOKEN, requestDto)).willReturn(expectedDto);

        EarnPointResponseDto result = pointUserClient.earnReviewPoint(TOKEN, requestDto);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("주문 적립 성공")
    void earnOrderPoint_Success() {
        EarnOrderPointRequestDto requestDto = mock(EarnOrderPointRequestDto.class);
        EarnPointResponseDto expectedDto = mock(EarnPointResponseDto.class);
        given(pointUserClient.earnOrderPoint(TOKEN, requestDto)).willReturn(expectedDto);

        EarnPointResponseDto result = pointUserClient.earnOrderPoint(TOKEN, requestDto);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("포인트 사용 성공")
    void usePoint_Success() {
        UsePointRequestDto requestDto = mock(UsePointRequestDto.class);
        EarnPointResponseDto expectedDto = mock(EarnPointResponseDto.class);
        given(pointUserClient.usePoint(TOKEN, requestDto)).willReturn(expectedDto);

        EarnPointResponseDto result = pointUserClient.usePoint(TOKEN, requestDto);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("포인트 반환 성공")
    void refundPoint_Success() {
        RefundPointRequestDto requestDto = mock(RefundPointRequestDto.class);
        EarnPointResponseDto expectedDto = mock(EarnPointResponseDto.class);
        given(pointUserClient.refundPoint(TOKEN, requestDto)).willReturn(expectedDto);

        EarnPointResponseDto result = pointUserClient.refundPoint(TOKEN, requestDto);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("소멸 예정 포인트 조회 성공")
    void getExpiringPoints_Success() {
        ExpiringPointResponseDto expectedDto = mock(ExpiringPointResponseDto.class);
        given(pointUserClient.getExpiringPoints(eq(TOKEN), anyInt())).willReturn(expectedDto);

        ExpiringPointResponseDto result = pointUserClient.getExpiringPoints(TOKEN, 7);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("포인트 내역 요약 조회 성공")
    void getPointSummary_Success() {
        PointSummaryResponseDto expectedDto = mock(PointSummaryResponseDto.class);
        given(pointUserClient.getPointSummary(TOKEN)).willReturn(expectedDto);

        PointSummaryResponseDto result = pointUserClient.getPointSummary(TOKEN);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("인증 토큰이 유효하지 않을 때 예외 발생")
    void getPointSummary_Unauthorized() {
        given(pointUserClient.getPointSummary(anyString()))
                .willThrow(FeignException.Unauthorized.class);

        assertThatThrownBy(() -> pointUserClient.getPointSummary("invalid-token"))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("적립 요청 중 서버 에러 발생 시 예외 발생")
    void earnOrderPoint_ServerError() {
        given(pointUserClient.earnOrderPoint(anyString(), any(EarnOrderPointRequestDto.class)))
                .willThrow(FeignException.InternalServerError.class);

        assertThatThrownBy(() -> pointUserClient.earnOrderPoint(TOKEN, new EarnOrderPointRequestDto()))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("포인트 사용액이 부족하거나 요청이 잘못되었을 때 예외 발생")
    void usePoint_BadRequest() {
        given(pointUserClient.usePoint(anyString(), any(UsePointRequestDto.class)))
                .willThrow(FeignException.BadRequest.class);

        assertThatThrownBy(() -> pointUserClient.usePoint(TOKEN, new UsePointRequestDto()))
                .isInstanceOf(FeignException.class);
    }
}