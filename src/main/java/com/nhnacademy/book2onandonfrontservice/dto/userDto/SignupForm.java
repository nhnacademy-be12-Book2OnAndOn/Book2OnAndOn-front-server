package com.nhnacademy.book2onandonfrontservice.dto.userDto;

import com.nhnacademy.book2onandonfrontservice.dto.userDto.enums.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class SignupForm {
    Long userId;
    String userLoginId; // 로그인 아이디
    String name;
    String email;
    String phone;
    String nickname;
    Role role;
    String gradeName;
}
