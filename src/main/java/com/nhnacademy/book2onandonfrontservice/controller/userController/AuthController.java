package com.nhnacademy.book2onandonfrontservice.controller.userController;


import com.nhnacademy.book2onandonfrontservice.dto.userDto.SignupForm;
import com.nhnacademy.book2onandonfrontservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           Model model) {
        if (error != null) {
            model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        if (logout != null) {
            model.addAttribute("logout", "로그아웃되었습니다.");
        }
        return "login";
    }

    /**
     * 회원가입 페이지
     */
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupForm", new SignupForm());
        return "signup";
    }

    /**
     * 회원가입 처리
     */
    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute SignupForm signupForm,
                        BindingResult bindingResult,
                        Model model) {
        
        // 유효성 검사 실패
        if (bindingResult.hasErrors()) {
            return "signup";
        }

        // 비밀번호 일치 확인
//        if (!signupForm.isPasswordMatch()) {
//            bindingResult.rejectValue("passwordConfirm", "passwordInCorrect",
//                "비밀번호가 일치하지 않습니다.");
//            return "signup";
//        }

        // 이메일 중복 확인
        if (userService.isEmailExists(signupForm.getEmail())) {
            bindingResult.rejectValue("email", "emailDuplicate", 
                "이미 사용 중인 이메일입니다.");
            return "signup";
        }

        try {
            // 회원가입 처리
            userService.signup(signupForm);
            return "redirect:/login?signup=success";
        } catch (Exception e) {
            model.addAttribute("error", "회원가입 중 오류가 발생했습니다.");
            return "signup";
        }
    }

    /**
     * 비밀번호 찾기 페이지
     */
    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot-password";
    }

    /**
     * 비밀번호 찾기 처리
     */
    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, Model model) {
        try {
//            userService.sendPasswordResetEmail(email);
            model.addAttribute("message", "비밀번호 재설정 이메일이 발송되었습니다.");
        } catch (Exception e) {
            model.addAttribute("error", "이메일 발송 중 오류가 발생했습니다.");
        }
        return "forgot-password";
    }

    /**
     * 로그아웃 (Spring Security가 처리)
     */
    @PostMapping("/logout")
    public String logout() {
        return "redirect:/login?logout";
    }
}