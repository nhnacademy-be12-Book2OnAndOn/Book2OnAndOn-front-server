package service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.TokenResponseDto;
import com.nhnacademy.book2onandonfrontservice.service.FrontTokenService;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class FrontTokenServiceTest {

    private FrontTokenService frontTokenService;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletRequestAttributes attributes;
    private MockedStatic<RequestContextHolder> mockedRequestContextHolder;
    private MockedStatic<CookieUtils> mockedCookieUtils;

    @BeforeEach
    void setUp() {
        frontTokenService = new FrontTokenService();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        attributes = mock(ServletRequestAttributes.class);

        mockedRequestContextHolder = mockStatic(RequestContextHolder.class);
        mockedCookieUtils = mockStatic(CookieUtils.class);

        mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
        when(attributes.getRequest()).thenReturn(request);
        when(attributes.getResponse()).thenReturn(response);
    }

    @AfterEach
    void tearDown() {
        mockedRequestContextHolder.close();
        mockedCookieUtils.close();
    }

    @Test
    @DisplayName("액세스 토큰 조회 - Bearer 접두어 제거 확인")
    void getAccessToken_ValidCookie() {
        String rawValue = "Bearer testtoken";
        String encodedValue = URLEncoder.encode(rawValue, StandardCharsets.UTF_8);
        mockedCookieUtils.when(() -> CookieUtils.getCookieValue(request, "accessToken")).thenReturn(encodedValue);

        String result = frontTokenService.getAccessToken();

        assertThat(result).isEqualTo("testtoken");
    }

    @Test
    @DisplayName("액세스 토큰 조회 - 접두어 없는 경우 확인")
    void getAccessToken_NoBearerPrefix() {
        mockedCookieUtils.when(() -> CookieUtils.getCookieValue(request, "accessToken")).thenReturn("plainToken");

        String result = frontTokenService.getAccessToken();

        assertThat(result).isEqualTo("plainToken");
    }

    @Test
    @DisplayName("액세스 토큰 조회 - 쿠키 부재 시 확인")
    void getAccessToken_NoCookie() {
        mockedCookieUtils.when(() -> CookieUtils.getCookieValue(request, "accessToken")).thenReturn(null);

        String result = frontTokenService.getAccessToken();

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("액세스 토큰 조회 - 컨텍스트 부재 시 확인")
    void getAccessToken_NoRequest() {
        mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

        String result = frontTokenService.getAccessToken();

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("리프레시 토큰 조회 - 정상 복호화 확인")
    void getRefreshToken_ValidCookie() {
        String rawValue = "refresh-token-value";
        String encodedValue = URLEncoder.encode(rawValue, StandardCharsets.UTF_8);
        mockedCookieUtils.when(() -> CookieUtils.getCookieValue(request, "refreshToken")).thenReturn(encodedValue);

        String result = frontTokenService.getRefreshToken();

        assertThat(result).isEqualTo(rawValue);
    }

    @Test
    @DisplayName("리프레시 토큰 조회 - 쿠키 부재 시 확인")
    void getRefreshToken_NoCookie() {
        mockedCookieUtils.when(() -> CookieUtils.getCookieValue(request, "refreshToken")).thenReturn(null);

        String result = frontTokenService.getRefreshToken();

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("토큰 업데이트 - 쿠키 생성 헤더 전송 확인")
    void updateTokens_Success() {
        TokenResponseDto tokenResponse = new TokenResponseDto("new-access", "new-refresh", "Bearer", "3600");

        frontTokenService.updateTokens(tokenResponse);

        verify(response, atLeastOnce()).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
    }

    @Test
    @DisplayName("토큰 업데이트 - 응답 객체 부재 시 확인")
    void updateTokens_NoResponse() {
        when(attributes.getResponse()).thenReturn(null);
        TokenResponseDto tokenResponse = new TokenResponseDto("a", "b", "c", "d");

        frontTokenService.updateTokens(tokenResponse);

        verify(response, times(0)).addHeader(anyString(), anyString());
    }

    @Test
    @DisplayName("토큰 삭제 - 만료된 쿠키 설정 확인")
    void clearTokens_Success() {
        frontTokenService.clearTokens();

        verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
    }

    @Test
    @DisplayName("토큰 디코딩 예외 발생 시 null 반환 확인")
    void getAccessToken_DecodeException() {
        mockedCookieUtils.when(() -> CookieUtils.getCookieValue(request, "accessToken")).thenReturn("%");

        String result = frontTokenService.getAccessToken();

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("리프레시 토큰 디코딩 예외 발생 시 null 반환 확인")
    void getRefreshToken_DecodeException() {
        mockedCookieUtils.when(() -> CookieUtils.getCookieValue(request, "refreshToken")).thenReturn("%");

        String result = frontTokenService.getRefreshToken();

        assertThat(result).isNull();
    }
}