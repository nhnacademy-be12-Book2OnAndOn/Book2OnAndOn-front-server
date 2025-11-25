package com.nhnacademy.book2onandonfrontservice.service;

import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.SignupForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserClient userClient; // Feign Client 주입
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화

    public void signup(SignupForm form) {
        // 비밀번호 암호화 후 전송 (선택 사항: 백엔드에서 할 수도 있음)
//        form.setPassword(passwordEncoder.encode(form.getPassword()));
        userClient.signup(form);
    }

    public boolean isEmailExists(String email) {
        return userClient.checkEmail(email);
    }

    // 비밀번호 찾기 로직 등 추가...
}