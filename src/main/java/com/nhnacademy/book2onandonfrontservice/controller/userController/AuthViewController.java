package com.nhnacademy.book2onandonfrontservice.controller.userController;

import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.FindIdRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.FindPasswordRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.LocalSignUpRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.LoginRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.FindIdResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.TokenResponseDto;
import jakarta.servlet.http.Cookie;
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
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return "redirect:/login?logout";
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