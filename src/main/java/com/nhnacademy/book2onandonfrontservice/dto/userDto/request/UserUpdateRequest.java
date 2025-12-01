package com.nhnacademy.book2onandonfrontservice.dto.userDto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    private String name;
    private String email;
    private String phone;
    private String nickname;
}
