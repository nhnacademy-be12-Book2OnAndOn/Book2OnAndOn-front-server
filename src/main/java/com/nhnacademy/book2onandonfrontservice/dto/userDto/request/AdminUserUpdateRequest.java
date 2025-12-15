package com.nhnacademy.book2onandonfrontservice.dto.userDto.request;

public record AdminUserUpdateRequest(
        String role,
        String status,
        String gradeName
) {
}
