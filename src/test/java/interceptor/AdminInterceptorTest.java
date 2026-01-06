package interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.nhnacademy.book2onandonfrontservice.interceptor.AdminInterceptor;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import com.nhnacademy.book2onandonfrontservice.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class AdminInterceptorTest {

    private AdminInterceptor adminInterceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Object handler;

    private MockedStatic<CookieUtils> cookieUtils;
    private MockedStatic<JwtUtils> jwtUtils;

    @BeforeEach
    void setUp() {
        adminInterceptor = new AdminInterceptor();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        handler = new Object();

        cookieUtils = mockStatic(CookieUtils.class);
        jwtUtils = mockStatic(JwtUtils.class);
    }

    @AfterEach
    void tearDown() {
        cookieUtils.close();
        jwtUtils.close();
    }

    @Test
    @DisplayName("토큰이 없으면 로그인 페이지로 리다이렉트하고 false를 반환한다")
    void preHandle_NoToken_RedirectToLogin() throws Exception {
        cookieUtils.when(() -> CookieUtils.getCookieValue(request, "accessToken")).thenReturn(null);

        boolean result = adminInterceptor.preHandle(request, response, handler);

        assertThat(result).isFalse();
        verify(response).sendRedirect("/login");
    }

    @Test
    @DisplayName("관리자 권한(ADMIN)이 없으면 메인으로 리다이렉트한다")
    void preHandle_NoAdminRole_RedirectToMain() throws Exception {
        cookieUtils.when(() -> CookieUtils.getCookieValue(request, "accessToken")).thenReturn("token");
        jwtUtils.when(() -> JwtUtils.getRole("token")).thenReturn("ROLE_USER");
        
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        boolean result = adminInterceptor.preHandle(request, response, handler);

        assertThat(result).isFalse();
        assertThat(stringWriter.toString()).contains("관리자 페이지 접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("SUPER_ADMIN은 모든 페이지를 통과한다")
    void preHandle_SuperAdmin_PassAll() throws Exception {
        cookieUtils.when(() -> CookieUtils.getCookieValue(request, "accessToken")).thenReturn("token");
        jwtUtils.when(() -> JwtUtils.getRole("token")).thenReturn("ROLE_SUPER_ADMIN");
        when(request.getRequestURI()).thenReturn("/admin/users");

        boolean result = adminInterceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("도서 관리자는 도서 관리 페이지 접근이 가능하다")
    void preHandle_BookAdmin_AccessBooks_Success() throws Exception {
        cookieUtils.when(() -> CookieUtils.getCookieValue(request, "accessToken")).thenReturn("token");
        jwtUtils.when(() -> JwtUtils.getRole("token")).thenReturn("ROLE_BOOK_ADMIN");
        when(request.getRequestURI()).thenReturn("/admin/books/register");

        boolean result = adminInterceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("도서 관리자가 회원 관리 페이지 접근 시 차단된다")
    void preHandle_BookAdmin_AccessUsers_Fail() throws Exception {
        cookieUtils.when(() -> CookieUtils.getCookieValue(request, "accessToken")).thenReturn("token");
        jwtUtils.when(() -> JwtUtils.getRole("token")).thenReturn("ROLE_BOOK_ADMIN");
        when(request.getRequestURI()).thenReturn("/admin/users");

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        boolean result = adminInterceptor.preHandle(request, response, handler);

        assertThat(result).isFalse();
        assertThat(stringWriter.toString()).contains("해당 메뉴에 대한 접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("주문 관리자는 주문/배송 관련 페이지 접근이 가능하다")
    void preHandle_OrderAdmin_AccessOrders_Success() throws Exception {
        cookieUtils.when(() -> CookieUtils.getCookieValue(request, "accessToken")).thenReturn("token");
        jwtUtils.when(() -> JwtUtils.getRole("token")).thenReturn("ROLE_ORDER_ADMIN");
        when(request.getRequestURI()).thenReturn("/admin/deliveries");

        boolean result = adminInterceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
    }
}