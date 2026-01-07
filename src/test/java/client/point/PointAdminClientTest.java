package client.point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.nhnacademy.book2onandonfrontservice.client.PointAdminClient;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.CurrentPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.EarnPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointHistoryAdminAdjustRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointHistoryResponseDto;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

@ExtendWith(MockitoExtension.class)
class PointAdminClientTest {

    @Mock
    private PointAdminClient pointAdminClient;

    private final String TOKEN = "Bearer admin-token";

    @Test
    @DisplayName("특정 유저 포인트 전체 이력 조회 성공")
    void getUserPointHistory_Success() {
        @SuppressWarnings("unchecked")
        Page<PointHistoryResponseDto> expectedPage = mock(Page.class);
        given(pointAdminClient.getUserPointHistory(eq(TOKEN), anyLong(), anyInt(), anyInt()))
                .willReturn(expectedPage);

        Page<PointHistoryResponseDto> result = pointAdminClient.getUserPointHistory(TOKEN, 1L, 0, 10);

        assertThat(result).isEqualTo(expectedPage);
        verify(pointAdminClient).getUserPointHistory(TOKEN, 1L, 0, 10);
    }

    @Test
    @DisplayName("특정 유저 현재 포인트 조회 성공")
    void getUserCurrentPoint_Success() {
        CurrentPointResponseDto expectedDto = mock(CurrentPointResponseDto.class);
        given(pointAdminClient.getUserCurrentPoint(TOKEN, 1L)).willReturn(expectedDto);

        CurrentPointResponseDto result = pointAdminClient.getUserCurrentPoint(TOKEN, 1L);

        assertThat(result).isEqualTo(expectedDto);
        verify(pointAdminClient).getUserCurrentPoint(TOKEN, 1L);
    }

    @Test
    @DisplayName("관리자 포인트 수동 조정 성공")
    void adjustPointByAdmin_Success() {
        PointHistoryAdminAdjustRequestDto requestDto = mock(PointHistoryAdminAdjustRequestDto.class);
        EarnPointResponseDto expectedDto = mock(EarnPointResponseDto.class);
        given(pointAdminClient.adjustPointByAdmin(TOKEN, requestDto)).willReturn(expectedDto);

        EarnPointResponseDto result = pointAdminClient.adjustPointByAdmin(TOKEN, requestDto);

        assertThat(result).isEqualTo(expectedDto);
        verify(pointAdminClient).adjustPointByAdmin(TOKEN, requestDto);
    }

    @Test
    @DisplayName("관리자 권한 없이 접근 시 예외 발생")
    void getUserPointHistory_Unauthorized() {
        given(pointAdminClient.getUserPointHistory(anyString(), anyLong(), anyInt(), anyInt()))
                .willThrow(FeignException.Unauthorized.class);

        assertThatThrownBy(() -> pointAdminClient.getUserPointHistory("invalid-token", 1L, 0, 10))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("존재하지 않는 유저 조회 시 예외 발생")
    void getUserCurrentPoint_NotFound() {
        given(pointAdminClient.getUserCurrentPoint(anyString(), anyLong()))
                .willThrow(FeignException.NotFound.class);

        assertThatThrownBy(() -> pointAdminClient.getUserCurrentPoint(TOKEN, 999L))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("포인트 조정 요청 데이터가 유효하지 않을 때 예외 발생")
    void adjustPointByAdmin_BadRequest() {
        given(pointAdminClient.adjustPointByAdmin(anyString(), any(PointHistoryAdminAdjustRequestDto.class)))
                .willThrow(FeignException.BadRequest.class);

        assertThatThrownBy(() -> pointAdminClient.adjustPointByAdmin(TOKEN, new PointHistoryAdminAdjustRequestDto()))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("서버 내부 오류 발생 시 예외 발생")
    void adjustPointByAdmin_ServerError() {
        given(pointAdminClient.adjustPointByAdmin(anyString(), any(PointHistoryAdminAdjustRequestDto.class)))
                .willThrow(FeignException.InternalServerError.class);

        assertThatThrownBy(() -> pointAdminClient.adjustPointByAdmin(TOKEN, new PointHistoryAdminAdjustRequestDto()))
                .isInstanceOf(FeignException.class);
    }
}