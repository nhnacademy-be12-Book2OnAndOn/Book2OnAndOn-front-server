package controller.usercontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.controller.userController.AuthViewController;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.LoginRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.FindIdResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.TokenResponseDto;
import com.nhnacademy.book2onandonfrontservice.service.FrontTokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

class AuthViewControllerTest {

    private MockMvc mockMvc;

    @Mock private UserClient userClient;
    @Mock private BookClient bookClient;
    @Mock private FrontTokenService frontTokenService;

    @InjectMocks private AuthViewController authViewController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(authViewController, "paycoClientId", "test-client-id");
        ReflectionTestUtils.setField(authViewController, "paycoAuthUri", "https://payco.auth");
        ReflectionTestUtils.setField(authViewController, "paycoRedirectUri", "https://redirect.uri");

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(authViewController)
                .setViewResolvers(viewResolver)
                // Validator를 등록하지 않거나 가짜를 등록하여 검증 에러를 무시하게 함
                .setValidator(mock(org.springframework.validation.Validator.class))
                .build();
    }

    @Test
    @DisplayName("로그인 폼 조회 - PAYCO URL 포함 확인")
    void loginForm_View() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeExists("paycoLoginUrl"));
    }

    @Test
    @DisplayName("로그인 성공 - 쿠키 발급 및 메인 리다이렉트")
    void login_Success() throws Exception {
        TokenResponseDto tokenResponse = new TokenResponseDto(
                "access-token-value",
                "refresh-token-value",
                "Bearer",
                "3600"
        );

        given(userClient.login(any(LoginRequest.class))).willReturn(tokenResponse);

        mockMvc.perform(post("/login")
                        .param("userLoginId", "testuser")
                        .param("userPassword", "password")
                        .param("rememberMe", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(cookie().value("accessToken", "access-token-value"))
                .andExpect(cookie().value("refreshToken", "refresh-token-value"));
    }

    @Test
    @DisplayName("회원가입 성공 - 로그인 페이지로 리다이렉트")
    void signup_Success() throws Exception {
        given(userClient.signUp(any())).willReturn(null);

        mockMvc.perform(post("/signup")
                        // 파라미터 이름을 DTO 필드명과 정확히 일치시켜야 함
                        .param("userLoginId", "testuser123")
                        .param("userPassword", "Password123!")
                        .param("userName", "홍길동")
                        .param("userNickname", "길동이")
                        .param("userEmail", "test@nhnacademy.com")
                        .param("userBirth", "1995-05-05")
                        .param("userPhone", "010-1234-5678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("아이디 찾기 성공")
    void findId_Success() throws Exception {
        FindIdResponseDto responseDto = new FindIdResponseDto("foundId");
        given(userClient.findId(any())).willReturn(responseDto);

        mockMvc.perform(post("/find-id")
                        .param("name", "홍길동")
                        .param("email", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/find-id-result"))
                .andExpect(model().attribute("userLoginId", "foundId"));
    }

    @Test
    @DisplayName("로그아웃 - 쿠키 삭제 및 서비스 호출")
    void logout_Process() throws Exception {
        given(frontTokenService.getAccessToken()).willReturn("valid-token");
        doNothing().when(userClient).logout(anyString());

        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"))
                .andExpect(cookie().maxAge("guestId", 0));
    }

    @Test
    @DisplayName("PAYCO 콜백 성공 - 자동 로그인 및 리다이렉트")
    void paycoCallback_Success() throws Exception {
        TokenResponseDto tokenResponse = new TokenResponseDto(
                "payco-access",
                "payco-refresh",
                "Bearer",
                "3600"
        );

        given(userClient.loginWithPayco(any())).willReturn(tokenResponse);

        mockMvc.perform(get("/login/oauth2/code/payco")
                        .param("code", "payco-auth-code"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(cookie().value("accessToken", "payco-access"));
    }

    @Test
    @DisplayName("아이디 중복 체크 API")
    void checkLoginId_Api() throws Exception {
        given(userClient.checkLoginId("testuser")).willReturn(true);

        mockMvc.perform(get("/check-id")
                        .param("userLoginId", "testuser"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}