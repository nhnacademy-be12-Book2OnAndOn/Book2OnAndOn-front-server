package com.nhnacademy.book2onandonfrontservice.service;

import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.TokenResponseDto;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class FrontTokenService {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "Authorization";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    public String getRefreshToken() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        return CookieUtils.getCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);
    }

    public void updateTokens(TokenResponseDto tokenResponse) {
        HttpServletResponse response = getCurrentResponse();
        if (response == null) {
            return;
        }

        String accessTokenValue = "Bearer " + tokenResponse.getAccessToken();
        String encodedAccessToken = URLEncoder.encode(accessTokenValue, StandardCharsets.UTF_8);

        addCookie(response, ACCESS_TOKEN_COOKIE_NAME, encodedAccessToken, 1800); // 30분

        String encodedRefreshToken = URLEncoder.encode(tokenResponse.getRefreshToken(), StandardCharsets.UTF_8);

        addCookie(response, REFRESH_TOKEN_COOKIE_NAME, encodedRefreshToken, 60 * 60 * 24 * 7); // 7일
    }

    // 쿠키 생성 헬퍼
    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
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