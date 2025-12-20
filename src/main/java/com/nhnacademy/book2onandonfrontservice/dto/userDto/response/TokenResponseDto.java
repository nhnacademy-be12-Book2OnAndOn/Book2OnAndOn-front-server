package com.nhnacademy.book2onandonfrontservice.dto.userDto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType; // "Bearer"
    private String expiresIn; // 만료시간
}