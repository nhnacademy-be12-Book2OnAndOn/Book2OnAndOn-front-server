package com.nhnacademy.book2onandonfrontservice.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class CookieUtils {
    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookieName)) {
                    String raw = cookie.getValue();
                    return raw != null ? URLDecoder.decode(raw, StandardCharsets.UTF_8) : null;
                }
            }
        }
        return null;
    }
}
