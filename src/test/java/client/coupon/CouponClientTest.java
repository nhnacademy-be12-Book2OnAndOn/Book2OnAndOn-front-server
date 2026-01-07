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

import com.nhnacademy.book2onandonfrontservice.client.CouponClient;
import com.nhnacademy.book2onandonfrontservice.dto.couponDto.CouponCreateDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponDto.CouponDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponDto.CouponUpdateDto;
import feign.FeignException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

@ExtendWith(MockitoExtension.class)
class CouponClientTest {

    @Mock
    private CouponClient couponClient;

    private final String TOKEN = "Bearer test-token";

    @Test
    @DisplayName("쿠폰 생성 성공")
    void createCoupon_Success() {
        CouponCreateDto requestDto = new CouponCreateDto();
        couponClient.createCoupon(TOKEN, requestDto);
        verify(couponClient).createCoupon(eq(TOKEN), any(CouponCreateDto.class));
    }

    @Test
    @DisplayName("쿠폰 목록 조회 성공")
    void getCoupons_Success() {
        @SuppressWarnings("unchecked")
        Page<CouponDto> expectedPage = mock(Page.class);
        given(couponClient.getCoupons(eq(TOKEN), anyInt(), anyInt(), anyString()))
                .willReturn(expectedPage);

        Page<CouponDto> result = couponClient.getCoupons(TOKEN, 0, 10, "ACTIVE");

        assertThat(result).isEqualTo(expectedPage);
    }

    @Test
    @DisplayName("쿠폰 수량 수정 성공")
    void updateCouponQuantity_Success() {
        CouponUpdateDto updateDto = new CouponUpdateDto();
        Long couponId = 1L;
        couponClient.updateCouponQuantity(TOKEN, couponId, updateDto);
        verify(couponClient).updateCouponQuantity(eq(TOKEN), eq(couponId), any(CouponUpdateDto.class));
    }

    @Test
    @DisplayName("적용 가능한 쿠폰 목록 조회 성공")
    void getAppliableCoupons_Success() {
        List<CouponDto> expectedList = List.of(mock(CouponDto.class));
        given(couponClient.getAppliableCoupons(eq(TOKEN), anyLong(), any()))
                .willReturn(expectedList);

        List<CouponDto> result = couponClient.getAppliableCoupons(TOKEN, 1L, List.of(10L, 20L));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("사용자 쿠폰 발급 성공")
    void issueCoupon_Success() {
        Long couponId = 1L;
        couponClient.issueCoupon(TOKEN, couponId);
        verify(couponClient).issueCoupon(TOKEN, couponId);
    }

    @Test
    @DisplayName("인증되지 않은 사용자의 쿠폰 발급 요청 시 예외 발생")
    void issueCoupon_Unauthorized() {
        doThrow(FeignException.Unauthorized.class)
                .when(couponClient).issueCoupon(anyString(), anyLong());

        assertThatThrownBy(() -> couponClient.issueCoupon("invalid-token", 1L))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰 조회 시 예외 발생")
    void getCoupons_NotFound() {
        given(couponClient.getCoupons(anyString(), anyInt(), anyInt(), anyString()))
                .willThrow(FeignException.NotFound.class);

        assertThatThrownBy(() -> couponClient.getCoupons(TOKEN, 0, 10, "NONE"))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("쿠폰 생성 중 서버 오류 발생 시 예외 발생")
    void createCoupon_ServerError() {
        doThrow(FeignException.InternalServerError.class)
                .when(couponClient).createCoupon(anyString(), any(CouponCreateDto.class));

        assertThatThrownBy(() -> couponClient.createCoupon(TOKEN, new CouponCreateDto()))
                .isInstanceOf(FeignException.class);
    }
}