package client.point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.nhnacademy.book2onandonfrontservice.client.PointPolicyAdminClient;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointPolicy.PointPolicyActiveUpdateRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointPolicy.PointPolicyResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointPolicy.PointPolicyUpdateRequestDto;
import feign.FeignException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PointPolicyAdminClientTest {

    @Mock
    private PointPolicyAdminClient pointPolicyAdminClient;

    private final String TOKEN = "Bearer admin-token";

    @Test
    @DisplayName("전체 포인트 정책 목록 조회 성공")
    void getAllPolicies_Success() {
        List<PointPolicyResponseDto> expectedList = List.of(mock(PointPolicyResponseDto.class));
        given(pointPolicyAdminClient.getAllPolicies(TOKEN)).willReturn(expectedList);

        List<PointPolicyResponseDto> result = pointPolicyAdminClient.getAllPolicies(TOKEN);

        assertThat(result).hasSize(1);
        verify(pointPolicyAdminClient).getAllPolicies(TOKEN);
    }

    @Test
    @DisplayName("정책 이름으로 단건 조회 성공")
    void getPolicy_Success() {
        String policyName = "SIGNUP";
        PointPolicyResponseDto expectedDto = mock(PointPolicyResponseDto.class);
        given(pointPolicyAdminClient.getPolicy(policyName, TOKEN)).willReturn(expectedDto);

        PointPolicyResponseDto result = pointPolicyAdminClient.getPolicy(policyName, TOKEN);

        assertThat(result).isEqualTo(expectedDto);
        verify(pointPolicyAdminClient).getPolicy(policyName, TOKEN);
    }

    @Test
    @DisplayName("포인트 정책 수정 성공")
    void updatePolicy_Success() {
        Integer policyId = 1;
        PointPolicyUpdateRequestDto requestDto = mock(PointPolicyUpdateRequestDto.class);
        PointPolicyResponseDto expectedDto = mock(PointPolicyResponseDto.class);
        given(pointPolicyAdminClient.updatePolicy(policyId, requestDto, TOKEN)).willReturn(expectedDto);

        PointPolicyResponseDto result = pointPolicyAdminClient.updatePolicy(policyId, requestDto, TOKEN);

        assertThat(result).isEqualTo(expectedDto);
        verify(pointPolicyAdminClient).updatePolicy(policyId, requestDto, TOKEN);
    }

    @Test
    @DisplayName("정책 활성/비활성 상태 수정 성공")
    void updatePolicyActive_Success() {
        Integer policyId = 1;
        PointPolicyActiveUpdateRequestDto requestDto = mock(PointPolicyActiveUpdateRequestDto.class);
        PointPolicyResponseDto expectedDto = mock(PointPolicyResponseDto.class);
        given(pointPolicyAdminClient.updatePolicyActive(policyId, requestDto, TOKEN)).willReturn(expectedDto);

        PointPolicyResponseDto result = pointPolicyAdminClient.updatePolicyActive(policyId, requestDto, TOKEN);

        assertThat(result).isEqualTo(expectedDto);
        verify(pointPolicyAdminClient).updatePolicyActive(policyId, requestDto, TOKEN);
    }

    @Test
    @DisplayName("관리자 토큰이 유효하지 않을 때 예외 발생")
    void getAllPolicies_Unauthorized() {
        given(pointPolicyAdminClient.getAllPolicies(anyString()))
                .willThrow(FeignException.Unauthorized.class);

        assertThatThrownBy(() -> pointPolicyAdminClient.getAllPolicies("invalid-token"))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("존재하지 않는 정책 아이디 수정 시 예외 발생")
    void updatePolicy_NotFound() {
        given(pointPolicyAdminClient.updatePolicy(anyInt(), any(PointPolicyUpdateRequestDto.class), anyString()))
                .willThrow(FeignException.NotFound.class);

        assertThatThrownBy(() -> pointPolicyAdminClient.updatePolicy(999, new PointPolicyUpdateRequestDto(), TOKEN))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("수정 요청 데이터가 잘못되었을 때 예외 발생")
    void updatePolicy_BadRequest() {
        given(pointPolicyAdminClient.updatePolicy(anyInt(), any(PointPolicyUpdateRequestDto.class), anyString()))
                .willThrow(FeignException.BadRequest.class);

        assertThatThrownBy(() -> pointPolicyAdminClient.updatePolicy(1, new PointPolicyUpdateRequestDto(), TOKEN))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("서버 내부 오류로 정책 상태 수정 실패 시 예외 발생")
    void updatePolicyActive_ServerError() {
        given(pointPolicyAdminClient.updatePolicyActive(anyInt(), any(PointPolicyActiveUpdateRequestDto.class), anyString()))
                .willThrow(FeignException.InternalServerError.class);

        assertThatThrownBy(() -> pointPolicyAdminClient.updatePolicyActive(1, new PointPolicyActiveUpdateRequestDto(), TOKEN))
                .isInstanceOf(FeignException.class);
    }
}