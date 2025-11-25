package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.userDto.SignupForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "gateway-service", contextId = "userClient", url = "${gateway.base-url}")
public interface UserClient {

    // 백엔드 User Service로 회원가입 요청 전송
    @PostMapping("/api/users/signup")
    void signup(@RequestBody SignupForm signupForm);

    // 이메일 중복 체크
    @GetMapping("/api/users/check-email")
    boolean checkEmail(@RequestParam("email") String email);
}
