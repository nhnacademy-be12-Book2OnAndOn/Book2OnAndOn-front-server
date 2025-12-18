package com.nhnacademy.book2onandonfrontservice.service;

import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.TokenResponseDto;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class FrontTokenService {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    // AccessToken 꺼낼 때 디코딩
    public String getAccessToken() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        String cookieValue = CookieUtils.getCookieValue(request, ACCESS_TOKEN_COOKIE_NAME);

        if (StringUtils.hasText(cookieValue)) {
            try {
                // 인코딩된 쿠키 값(Bearer+)을 원래 값(Bearer )으로 복원
                String decodedValue = URLDecoder.decode(cookieValue, StandardCharsets.UTF_8);

                if (decodedValue.startsWith("Bearer ")) {
                    return decodedValue.substring(7);
                }
                return decodedValue;
            } catch (Exception e) {
                log.error("AccessToken 쿠키 디코딩 실패", e);
            }
        }
        return null;
    }

    // RefreshToken 꺼낼 때도 디코딩
    public String getRefreshToken() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        String cookieValue = CookieUtils.getCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);

        if (StringUtils.hasText(cookieValue)) {
            try {
                return URLDecoder.decode(cookieValue, StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.error("RefreshToken 쿠키 디코딩 실패", e);
            }
        }
        return null;
    }

    public void updateTokens(TokenResponseDto tokenResponse) {
        HttpServletResponse response = getCurrentResponse();
        if (response == null) {
            return;
        }

        // Access Token 저장

        String accessTokenValue = "Bearer " + tokenResponse.getAccessToken();
        String encodedAccessToken = URLEncoder.encode(accessTokenValue, StandardCharsets.UTF_8);

        addCookie(response, ACCESS_TOKEN_COOKIE_NAME, encodedAccessToken, 1800);

        // Refresh Token 저장
        String encodedRefreshToken = URLEncoder.encode(tokenResponse.getRefreshToken(), StandardCharsets.UTF_8);
        addCookie(response, REFRESH_TOKEN_COOKIE_NAME, encodedRefreshToken, 60 * 60 * 24 * 7); // 7일
    }

    // 로그아웃 시 쿠키 삭제를 위한 메서드
    public void clearTokens() {
        HttpServletResponse response = getCurrentResponse();
        if (response != null) {
            addCookie(response, ACCESS_TOKEN_COOKIE_NAME, "", 0);
            addCookie(response, REFRESH_TOKEN_COOKIE_NAME, "", 0);
        }
    }

    // 쿠키 생성 헬퍼
    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(true)
                .secure(true)     // HTTPS 환경 필수
                .maxAge(maxAge)
                .sameSite("None") // Cross-Site 요청 허용 (Secure=true와 짝꿍)
                .build();
//
        // 로컬용
//        ResponseCookie cookie = ResponseCookie.from(name, value)
//                .path("/")
//                .httpOnly(true)
//                .secure(false)
//                .maxAge(maxAge)
//                .sameSite("Lax")
//                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attr != null ? attr.getRequest() : null;
    }

    private HttpServletResponse getCurrentResponse() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attr != null ? attr.getResponse() : null;
    }
}