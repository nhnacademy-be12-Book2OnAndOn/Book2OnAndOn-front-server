package com.nhnacademy.book2onandonfrontservice.controller.userController;

import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.FindIdRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.FindPasswordRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.LocalSignUpRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.LoginRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.FindIdResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.TokenResponseDto;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final UserClient userClient;


    //로그인 페이지
    @GetMapping("/login")
    public String loginForm() {
        return "auth/login";
    }


    //로그인 처리
    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest loginRequest,
                        HttpServletResponse response,
                        Model model) {
        try {
            TokenResponseDto token = userClient.login(loginRequest);
            Cookie accessCookie = new Cookie("accessToken", token.getAccessToken());
            accessCookie.setHttpOnly(true);
            accessCookie.setSecure(true);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(1800);
            response.addCookie(accessCookie);

            Cookie refreshCookie = new Cookie("refreshToken", token.getRefreshToken());
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(604800); //7일
            response.addCookie(refreshCookie);

            return "redirect:/";

        } catch (Exception e) {
            model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "auth/login";
        }
    }


    //회원가입 페이지
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupForm", new LocalSignUpRequest());
        return "auth/signup";
    }


    //회원가입 처리
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

        } catch (Exception e) {
            log.error("회원가입 실패", e);
            model.addAttribute("error", "회원가입에 실패했습니다. (" + e.getMessage() + ")");
            return "auth/signup";
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

    // 쿠키 삭제 헬퍼 메서드
    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
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
}