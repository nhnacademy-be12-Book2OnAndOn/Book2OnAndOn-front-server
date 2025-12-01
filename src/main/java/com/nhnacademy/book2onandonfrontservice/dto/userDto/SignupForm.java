package com.nhnacademy.book2onandonfrontservice.dto.userDto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class SignupForm {
    @NotBlank(message = "아이디는 필수입니다.")
    private String userLoginId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @NotBlank(message = "전화번호는 필수입니다.")
    private String phone;

    private LocalDate birth;
}
