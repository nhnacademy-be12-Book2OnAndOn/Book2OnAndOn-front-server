package com.nhnacademy.book2onandonfrontservice.controller.userController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.FindIdRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.FindPasswordRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.LocalSignUpRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.LoginRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.PaycoLoginRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.FindIdResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.TokenResponseDto;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import feign.FeignException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final UserClient userClient;
    private final BookClient bookClient;

    @Value("${payco.client-id}")
    private String paycoClientId;

    @Value("${payco.authorization-uri}")
    private String paycoAuthUri;

    @Value("${payco.redirect-uri}")
    private String paycoRedirectUri;

    // Payco 로그인 페이지
    @GetMapping("/login")
    public String loginForm(Model model) {
        String paycoLoginUrl = String.format(
                "%s?response_type=code&client_id=%s&serviceProviderCode=FRIENDS&redirect_uri=%s&userLocale=ko_KR",
                paycoAuthUri, paycoClientId, paycoRedirectUri
        );

        model.addAttribute("paycoLoginUrl", paycoLoginUrl);
        return "auth/login";
    }


    //로그인 처리
    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest loginRequest,
                        HttpServletResponse response,
                        HttpServletRequest request,
                        Model model) {
        try {
            TokenResponseDto token = userClient.login(loginRequest);
            boolean secureRequest = isSecureRequest(request);

            // 쿠키 설정 (Access Token)
            Cookie accessCookie = new Cookie("accessToken", token.getAccessToken());
            accessCookie.setHttpOnly(true);
            accessCookie.setSecure(secureRequest);
            accessCookie.setPath("/");
            if (loginRequest.isRememberMe()) {
                accessCookie.setMaxAge(1800); // 30분
            } else {
                accessCookie.setMaxAge(-1); // 세션 쿠키
            }
            response.addCookie(accessCookie);

            // 쿠키 설정 (Refresh Token)
            Cookie refreshCookie = new Cookie("refreshToken", token.getRefreshToken());
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(secureRequest);
            refreshCookie.setPath("/");
            if (loginRequest.isRememberMe()) {
                refreshCookie.setMaxAge(604800); // 7일
            } else {
                refreshCookie.setMaxAge(-1);
            }
            response.addCookie(refreshCookie);

            // 최근 본 상품 병합 (비회원 -> 회원)
            try {
                String guestId = resolveGuestId(request);
                String bearerToken = "Bearer " + token.getAccessToken();

                if (guestId != null) {
                    bookClient.mergeRecentViews(bearerToken, guestId);
                    clearGuestIdCookie(response, secureRequest);
                }
            } catch (Exception e) {
                log.warn("로그인 후 최근 본 상품 병합 실패", e);
            }

            return "redirect:/";

        } catch (FeignException e) {
            log.error("로그인 실패", e);

            String errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다.";
            String errorCode = "";

            try {
                String responseBody = e.contentUTF8();
                if (responseBody != null && !responseBody.isBlank()) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> errorMap = objectMapper.readValue(responseBody, Map.class);

                    if (errorMap != null) {
                        if (errorMap.containsKey("message")) {
                            errorMessage = (String) errorMap.get("message");
                        }
                        if (errorMap.containsKey("error")) {
                            errorCode = (String) errorMap.get("error");
                        }
                    }
                }
            } catch (Exception parsingEx) {
                log.warn("에러 메시지 파싱 실패", parsingEx);
            }

            if ("ACCOUNT_DORMANT".equals(errorCode)) {
                return "redirect:/dormant";
            }

            model.addAttribute("error", errorMessage);
            return "auth/login";

        } catch (Exception e) {
            log.error("로그인 중 알 수 없는 오류", e);
            model.addAttribute("error", "로그인 처리 중 오류가 발생했습니다.");
            return "auth/login";
        }
    }


    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupForm", new LocalSignUpRequest());

        // PAYCO 인증 URL 생성 (로그인 페이지와 동일)
        String paycoLoginUrl = String.format(
                "%s?response_type=code&client_id=%s&serviceProviderCode=FRIENDS&redirect_uri=%s&userLocale=ko_KR",
                paycoAuthUri, paycoClientId, paycoRedirectUri
        );

        // 모델에 URL 추가
        model.addAttribute("paycoLoginUrl", paycoLoginUrl);

        return "auth/signup";
    }


    // 회원가입 처리
    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("signupForm") LocalSignUpRequest signupForm,
                         BindingResult bindingResult,
                         Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        try {
            userClient.signUp(signupForm);
            return "redirect:/login";

        } catch (FeignException e) {
            log.error("회원가입 API 호출 에러", e);

            String errorMessage = "회원가입에 실패했습니다.";

            try {
                // JSON 본문에서 message 추출 로직
                String responseBody = e.contentUTF8();
                if (responseBody != null && !responseBody.isBlank()) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> errorMap = objectMapper.readValue(responseBody, Map.class);

                    if (errorMap != null && errorMap.containsKey("message")) {
                        errorMessage += " (" + errorMap.get("message") + ")";
                    }
                }
            } catch (Exception parsingEx) {
                log.error("에러 메시지 파싱 실패", parsingEx);
            }

            model.addAttribute("error", errorMessage);
            return "auth/signup";

        } catch (Exception e) {
            log.error("회원가입 중 알 수 없는 오류", e);
            model.addAttribute("error", "회원가입에 실패했습니다. (시스템 오류)");
            return "auth/signup";
        }
    }

    // 아이디 중복 확인
    @GetMapping("/check-id")
    @ResponseBody
    public ResponseEntity<Boolean> checkLoginId(@RequestParam("userLoginId") String userLoginId) {
        try {
            boolean isDuplicate = userClient.checkLoginId(userLoginId);
            return ResponseEntity.ok(isDuplicate);
        } catch (Exception e) {
            log.error("아이디 중복 확인 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(true);
        }
    }

    // 닉네임 중복 확인
    @GetMapping("/check-nickname")
    @ResponseBody
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        try {
            boolean isDuplicate = userClient.checkNickname(nickname);
            return ResponseEntity.ok(isDuplicate);
        } catch (Exception e) {
            log.error("닉네임 중복 확인 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(true);
        }
    }

    //이메일 인증번호 발송
    @PostMapping("/email/send")
    @ResponseBody
    public ResponseEntity<String> sendEmail(@RequestParam String email) {
        try {
            userClient.sendEmailVerification(email);
            return ResponseEntity.ok("인증번호가 발송되었습니다.");
        } catch (FeignException e) {
            log.error("이메일 발송 API 호출 에러", e);
            String errorMessage = "발송 실패";

            try {
                String responseBody = e.contentUTF8();
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> errorMap = objectMapper.readValue(responseBody, Map.class);

                if (errorMap != null && errorMap.containsKey("message")) {
                    errorMessage = (String) errorMap.get("message");
                }
            } catch (Exception parsingEx) {
                log.error("에러 메시지 파싱 실패", parsingEx);
            }

            return ResponseEntity.badRequest().body(errorMessage);

        } catch (Exception e) {
            log.error("이메일 발송 중 알 수 없는 오류", e);
            return ResponseEntity.badRequest().body("시스템 오류가 발생했습니다.");
        }
    }

    //인증번호 검증
    @PostMapping("/email/verify")
    @ResponseBody
    public ResponseEntity<String> verifyCode(@RequestParam String email, @RequestParam String code) {
        try {
            String result = userClient.verifyEmail(email, code);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("인증 실패: 코드를 확인해주세요.");
        }
    }

    //로그아웃(쿠키 삭제)
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 Access Token 꺼내기
        String accessToken = CookieUtils.getCookieValue(request, "accessToken");

        // Redis 블랙리스트 등록
        if (accessToken != null) {
            try {
                // Bearer를 붙여서 보내야 Gateway/BE에서 인식
                userClient.logout("Bearer " + accessToken);
            } catch (Exception e) {
                // 백엔드 호출이 실패하더라도(이미 만료됨 등),
                // 프론트엔드에서는 무조건 로그아웃 처리를 진행해야 함.
                log.warn("백엔드 로그아웃 호출 실패 (토큰 만료 등): {}", e.getMessage());
            }
        }

        // 클라이언트 쿠키 강제 삭제
        deleteCookie(response, "accessToken");
        deleteCookie(response, "refreshToken");

        return "redirect:/login?logout";
    }

    private boolean isSecureRequest(HttpServletRequest request) {
        String proto = request.getHeader("X-Forwarded-Proto");
        return request.isSecure() || "https".equalsIgnoreCase(proto);
    }

    // 쿠키 삭제 헬퍼 메서드
    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private String resolveGuestId(HttpServletRequest request) {
        String gid = CookieUtils.getCookieValue(request, "GUEST_ID");
        if (gid == null) {
            gid = CookieUtils.getCookieValue(request, "guestId"); // 하위 호환
        }
        return gid;
    }

    private void clearGuestIdCookie(HttpServletResponse response, boolean secure) {
        deleteCookieWithSecure(response, "GUEST_ID", secure);
        deleteCookieWithSecure(response, "guestId", secure);
    }

    private void deleteCookieWithSecure(HttpServletResponse response, String name, boolean secure) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setSecure(secure);
        response.addCookie(cookie);
    }

    //아이디 찾기 페이지
    @GetMapping("/find-id")
    public String findIdForm() {
        return "auth/find-id";
    }

    //아이디 찾기 처리
    @PostMapping("/find-id")
    public String findId(
            @RequestParam String name,
            @RequestParam String email,
            Model model
    ) {
        try {
            FindIdResponseDto response = userClient.findId(
                    new FindIdRequest(name, email)
            );

            model.addAttribute("userLoginId", response.userLoginId());
            return "auth/find-id-result";
        } catch (Exception e) {
            model.addAttribute("error", "일치하는 회원 정보를 찾을 수 없습니다.");
            return "auth/find-id";
        }
    }


    //비밀번호 찾기 페이지
    @GetMapping("/find-password")
    public String findPasswordForm() {
        return "auth/find-password";
    }

    @PostMapping("/find-password")
    public String findPassword(
            @RequestParam String userLoginId,
            @RequestParam String email,
            Model model
    ) {
        try {
            userClient.findPassword(
                    new FindPasswordRequest(userLoginId, email)
            );
            return "auth/find-password-result";
        } catch (Exception e) {
            model.addAttribute("error", "회원 정보를 찾을 수 없습니다.");
            return "auth/find-password";
        }
    }


    // PAYCO Callback 처리
    @GetMapping("/login/oauth2/code/payco")
    public String paycoCallback(@RequestParam String code,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        try {
            // PAYCO 로그인 요청
            TokenResponseDto token = userClient.loginWithPayco(new PaycoLoginRequest(code));
            boolean secureRequest = isSecureRequest(request);

            // 쿠키 설정
            Cookie accessCookie = new Cookie("accessToken", token.getAccessToken());
            accessCookie.setHttpOnly(true);
            accessCookie.setSecure(secureRequest);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(1800);
            response.addCookie(accessCookie);

            Cookie refreshCookie = new Cookie("refreshToken", token.getRefreshToken());
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(secureRequest);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(604800);
            response.addCookie(refreshCookie);

            // 최근 본 상품 병합
            try {
                String guestId = resolveGuestId(request);
                String bearerToken = "Bearer " + token.getAccessToken();

                if (guestId != null) {
                    bookClient.mergeRecentViews(bearerToken, guestId);
                    clearGuestIdCookie(response, secureRequest);
                }
            } catch (Exception e) {
                log.warn("PAYCO 로그인 후 데이터 병합 실패", e);
            }

            return "redirect:/";

        } catch (FeignException e) {
            log.error("PAYCO 로그인 실패 (Feign)", e);
            String errorCode = "";
            String errorMessage = "";

            try {
                String responseBody = e.contentUTF8();
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> errorMap = objectMapper.readValue(responseBody, Map.class);

                if (errorMap != null) {
                    if (errorMap.containsKey("error")) {
                        errorCode = (String) errorMap.get("error");
                    }
                    if (errorMap.containsKey("message")) {
                        errorMessage = (String) errorMap.get("message");
                    }
                }
            } catch (Exception parsingEx) {
            }

            // [휴면 계정 체크]
            if ("ACCOUNT_DORMANT".equals(errorCode)) {
                return "redirect:/dormant";
            }

            // [정보 제공 미동의 체크]
            if ("PAYCO_INFO_MISSING".equals(errorCode) || errorMessage.contains("PAYCO_INFO_MISSING")) {
                return "redirect:/login?error=payco_consent";
            }

            return "redirect:/login?error=payco";

        } catch (Exception e) {
            log.error("PAYCO 로그인 중 알 수 없는 오류", e);
            return "redirect:/login?error=payco";
        }
    }

    //휴면 해제 페이지
    @GetMapping("/dormant")
    public String dormantForm() {
        return "auth/dormant";
    }

    // 휴면 해제 인증번호 발송
    @PostMapping("/dormant/email/send")
    @ResponseBody
    public ResponseEntity<String> sendDormantEmail(@RequestParam String email) {
        try {
            userClient.sendDormantVerification(email);
            return ResponseEntity.ok("인증번호가 발송되었습니다.");
        } catch (FeignException e) {
            log.error("휴면 인증번호 발송 실패", e);
            String errorMessage = "발송실패";
            try {
                String responseBody = e.contentUTF8();
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> errorMap = objectMapper.readValue(responseBody, Map.class);
                if (errorMap != null && errorMap.containsKey("message")) {
                    errorMessage = (String) errorMap.get("message");
                }
            } catch (Exception parsingEx) {
                //파싱 실패시 무시
            }
            return ResponseEntity.badRequest().body(errorMessage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("오류가 발생했습니다.");
        }
    }

    // 휴면 해제 처리
    @PostMapping("/dormant/unlock")
    @ResponseBody
    public ResponseEntity<String> unlockDormant(@RequestParam String email, @RequestParam String code) {
        try {
            userClient.unlockDormantAccount(email, code);
            return ResponseEntity.ok("휴면 상태가 해제되었습니다. 다시 로그인해주세요.");
        } catch (FeignException e) {
            log.error("휴면 해제 실패", e);
            String errorMessage = "해제 실패";
            try {
                String responseBody = e.contentUTF8();
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> errorMap = objectMapper.readValue(responseBody, Map.class);
                if (errorMap != null && errorMap.containsKey("message")) {
                    errorMessage = (String) errorMap.get("message");
                }
            } catch (Exception parsingEx) {
                // 파싱 실패 시 무시
            }
            return ResponseEntity.badRequest().body(errorMessage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("오류가 발생했습니다.");
        }
    }
}
