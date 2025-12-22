package com.nhnacademy.book2onandonfrontservice.dto.userDto.request;

import jakarta.validation.constraints.NotBlank;

public record ReissueRequestDto(
        @NotBlank String accessToken,
        @NotBlank String refreshToken
) {
}