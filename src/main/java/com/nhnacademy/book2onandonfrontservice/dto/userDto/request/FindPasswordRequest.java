package com.nhnacademy.book2onandonfrontservice.dto.userDto.request;

public record FindPasswordRequest(
        String userLoginId,
        String email
) {
}
