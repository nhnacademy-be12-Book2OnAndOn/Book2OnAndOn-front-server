package tokenreissue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.ReissueRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.TokenResponseDto;
import com.nhnacademy.book2onandonfrontservice.service.FrontTokenService;
import com.nhnacademy.book2onandonfrontservice.tokenreissue.TokenReissueAspect;
import com.nhnacademy.book2onandonfrontservice.util.JwtUtils;
import feign.FeignException;
import feign.Request;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;

class TokenReissueAspectTest {

    @InjectMocks
    private TokenReissueAspect tokenReissueAspect;

    @Mock
    private UserClient userClient;

    @Mock
    private FrontTokenService tokenService;

    private MockedStatic<JwtUtils> mockedJwtUtils;
    private MockedStatic<SecurityContextHolder> mockedSecurityContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockedJwtUtils = mockStatic(JwtUtils.class);
        mockedSecurityContext = mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    void tearDown() {
        mockedJwtUtils.close();
        mockedSecurityContext.close();
    }

    @Test
    @DisplayName("정상적인 클라이언트 호출 시 재발급 로직 없이 결과 반환")
    void handle401_Success() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        given(joinPoint.proceed()).willReturn("Success");

        Object result = tokenReissueAspect.handle401(joinPoint);

        assertThat(result).isEqualTo("Success");
        verify(userClient, never()).reissue(any());
    }

    @Test
    @DisplayName("401 발생 시 토큰 재발급 후 메서드 재실행")
    void handle401_Unauthorized_ReissueSuccess() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        given(joinPoint.getSignature()).willReturn(signature);
        given(signature.getName()).willReturn("someMethod");
        given(joinPoint.getArgs()).willReturn(new Object[]{"Bearer old"});

        FeignException.Unauthorized unauthorized = createFeignUnauthorized();
        given(joinPoint.proceed()).willThrow(unauthorized).willReturn("Retry Success");

        given(tokenService.getAccessToken()).willReturn("old");
        given(tokenService.getRefreshToken()).willReturn("refresh");

        TokenResponseDto tokenResponse = new TokenResponseDto("new", "new-refresh", "Bearer", "3600");
        given(userClient.reissue(any(ReissueRequestDto.class))).willReturn(tokenResponse);

        mockedJwtUtils.when(() -> JwtUtils.getUserId(anyString())).thenReturn(1L);
        mockedJwtUtils.when(() -> JwtUtils.getRole(anyString())).thenReturn("ROLE_USER");

        Object result = tokenReissueAspect.handle401(joinPoint);

        assertThat(result).isEqualTo("Retry Success");
        verify(tokenService).updateTokens(tokenResponse);
        verify(joinPoint, times(2)).proceed(any());
    }

    @Test
    @DisplayName("로그인 또는 재발급 메서드에서 401 발생 시 즉시 예외 전달")
    void handle401_LoginOrReissue_ThrowImmediate() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        given(joinPoint.getSignature()).willReturn(signature);
        given(signature.getName()).willReturn("reissue");

        given(joinPoint.proceed()).willThrow(createFeignUnauthorized());

        assertThatThrownBy(() -> tokenReissueAspect.handle401(joinPoint))
                .isInstanceOf(FeignException.Unauthorized.class);
    }

    @Test
    @DisplayName("게스트 카트 호출 시 401 발생하면 재발급 없이 예외 전달")
    void handle401_GuestCartCall_ThrowImmediate() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        given(joinPoint.getSignature()).willReturn(signature);
        given(signature.getName()).willReturn("guestAction");
        given(signature.toShortString()).willReturn("CartGuestClient.guestAction");

        given(joinPoint.proceed()).willThrow(createFeignUnauthorized());

        assertThatThrownBy(() -> tokenReissueAspect.handle401(joinPoint))
                .isInstanceOf(FeignException.Unauthorized.class);
    }

    @Test
    @DisplayName("리프레시 토큰이 없는 상태에서 401 발생 시 예외 전달")
    void handle401_NoRefreshToken_Throw() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        given(joinPoint.getSignature()).willReturn(signature);
        given(signature.getName()).willReturn("someMethod");

        given(joinPoint.proceed()).willThrow(createFeignUnauthorized());
        given(tokenService.getRefreshToken()).willReturn(null);

        assertThatThrownBy(() -> tokenReissueAspect.handle401(joinPoint))
                .isInstanceOf(FeignException.Unauthorized.class);
    }

    @Test
    @DisplayName("토큰 재발급 API 자체가 실패할 경우 예외 전달")
    void handle401_ReissueApiFail_Throw() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        given(joinPoint.getSignature()).willReturn(signature);
        given(signature.getName()).willReturn("someMethod");

        given(joinPoint.proceed()).willThrow(createFeignUnauthorized());
        given(tokenService.getRefreshToken()).willReturn("refresh");
        
        given(userClient.reissue(any())).willThrow(new RuntimeException("API Down"));

        assertThatThrownBy(() -> tokenReissueAspect.handle401(joinPoint))
                .isExactlyInstanceOf(RuntimeException.class);
    }

    private FeignException.Unauthorized createFeignUnauthorized() {
        return new FeignException.Unauthorized(
                "Unauthorized",
                mock(Request.class),
                null,
                Map.of()
        );
    }
}