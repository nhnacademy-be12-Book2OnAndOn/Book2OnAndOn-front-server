package client.coupon;

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

import com.nhnacademy.book2onandonfrontservice.client.CouponPolicyClient;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.CouponPolicyDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.CouponPolicyUpdateDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyDiscountType;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyStatus;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyType;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

@ExtendWith(MockitoExtension.class)
class CouponPolicyClientTest {

    @Mock
    private CouponPolicyClient couponPolicyClient;

    private final String TOKEN = "Bearer test-token";

    @Test
    @DisplayName("쿠폰 정책 페이징 목록 조회 성공")
    void getPolicies_Success() {
        @SuppressWarnings("unchecked")
        Page<CouponPolicyDto> expectedPage = mock(Page.class);
        given(couponPolicyClient.getPolicies(eq(TOKEN), anyInt(), anyInt(), any(), any(), any()))
                .willReturn(expectedPage);

        Page<CouponPolicyDto> result = couponPolicyClient.getPolicies(
                TOKEN, 0, 10, CouponPolicyType.BIRTHDAY, CouponPolicyDiscountType.FIXED, CouponPolicyStatus.ACTIVE
        );

        assertThat(result).isEqualTo(expectedPage);
    }

    @Test
    @DisplayName("쿠폰 정책 단건 조회 성공")
    void getPolicy_Success() {
        Long policyId = 1L;
        CouponPolicyDto expectedDto = mock(CouponPolicyDto.class);
        given(couponPolicyClient.getPolicy(TOKEN, policyId)).willReturn(expectedDto);

        CouponPolicyDto result = couponPolicyClient.getPolicy(TOKEN, policyId);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("쿠폰 정책 생성 성공")
    void createPolicy_Success() {
        CouponPolicyUpdateDto requestDto = mock(CouponPolicyUpdateDto.class);
        couponPolicyClient.createPolicy(TOKEN, requestDto);
        verify(couponPolicyClient).createPolicy(TOKEN, requestDto);
    }

    @Test
    @DisplayName("쿠폰 정책 수정 성공")
    void updatePolicy_Success() {
        Long policyId = 1L;
        CouponPolicyUpdateDto requestDto = mock(CouponPolicyUpdateDto.class);
        couponPolicyClient.updatePolicy(TOKEN, policyId, requestDto);
        verify(couponPolicyClient).updatePolicy(TOKEN, policyId, requestDto);
    }

    @Test
    @DisplayName("쿠폰 정책 비활성화 성공")
    void deactivatePolicy_Success() {
        Long policyId = 1L;
        couponPolicyClient.deactivatePolicy(TOKEN, policyId);
        verify(couponPolicyClient).deactivatePolicy(TOKEN, policyId);
    }

    @Test
    @DisplayName("정책 조회 시 권한이 없으면 예외 발생")
    void getPolicy_Unauthorized() {
        given(couponPolicyClient.getPolicy(anyString(), anyLong()))
                .willThrow(FeignException.Unauthorized.class);

        assertThatThrownBy(() -> couponPolicyClient.getPolicy("invalid", 1L))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("존재하지 않는 정책 조회 시 예외 발생")
    void getPolicy_NotFound() {
        given(couponPolicyClient.getPolicy(anyString(), anyLong()))
                .willThrow(FeignException.NotFound.class);

        assertThatThrownBy(() -> couponPolicyClient.getPolicy(TOKEN, 999L))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("정책 생성 중 서버 에러 발생 시 예외 발생")
    void createPolicy_ServerError() {
        doThrow(FeignException.InternalServerError.class)
                .when(couponPolicyClient).createPolicy(anyString(), any(CouponPolicyUpdateDto.class));

        assertThatThrownBy(() -> couponPolicyClient.createPolicy(TOKEN, new CouponPolicyUpdateDto()))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("정책 비활성화 중 접근 거부 시 예외 발생")
    void deactivatePolicy_Forbidden() {
        doThrow(FeignException.Forbidden.class)
                .when(couponPolicyClient).deactivatePolicy(anyString(), anyLong());

        assertThatThrownBy(() -> couponPolicyClient.deactivatePolicy(TOKEN, 1L))
                .isInstanceOf(FeignException.class);
    }
}