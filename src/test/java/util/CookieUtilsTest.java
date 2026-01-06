package util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CookieUtilsTest {

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
    }

    @Test
    @DisplayName("쿠키 목록이 null인 경우 null 반환 확인")
    void getCookieValue_CookiesNull() {
        when(request.getCookies()).thenReturn(null);

        String result = CookieUtils.getCookieValue(request, "anyName");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("찾으려는 이름의 쿠키가 없는 경우 null 반환 확인")
    void getCookieValue_CookieNotFound() {
        Cookie cookie1 = new Cookie("otherName", "value");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie1});

        String result = CookieUtils.getCookieValue(request, "targetName");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("쿠키가 존재하고 값이 일반 문자열인 경우 정상 반환 확인")
    void getCookieValue_Success_PlainValue() {
        String name = "testCookie";
        String value = "testValue";
        Cookie cookie = new Cookie(name, value);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        String result = CookieUtils.getCookieValue(request, name);

        assertThat(result).isEqualTo(value);
    }

    @Test
    @DisplayName("쿠키 값이 인코딩된 경우 정상적으로 디코딩하여 반환 확인")
    void getCookieValue_Success_EncodedValue() {
        String name = "auth";
        String originalValue = "Bearer test-token";
        String encodedValue = URLEncoder.encode(originalValue, StandardCharsets.UTF_8);
        
        Cookie cookie = new Cookie(name, encodedValue);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        String result = CookieUtils.getCookieValue(request, name);

        assertThat(result).isEqualTo(originalValue);
    }

    @Test
    @DisplayName("쿠키는 존재하지만 값이 null인 경우 null 반환 확인")
    void getCookieValue_ValueNull() {
        String name = "nullCookie";
        Cookie cookie = new Cookie(name, null);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        String result = CookieUtils.getCookieValue(request, name);

        assertThat(result).isNull();
    }
}