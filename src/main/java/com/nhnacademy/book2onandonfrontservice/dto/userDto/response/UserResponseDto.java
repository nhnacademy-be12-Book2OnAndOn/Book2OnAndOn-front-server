package com.nhnacademy.book2onandonfrontservice.dto.userDto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserResponseDto {
    private Long userId;
    private String userLoginId;
    private String name;
    private String email;
    private String phone;
    private String nickname;
    @JsonAlias({"point", "totalPoint", "currentPoint"})
    private Integer point;
    private String role;
    private String gradeName;
    private String status;
    private String provider;
}
