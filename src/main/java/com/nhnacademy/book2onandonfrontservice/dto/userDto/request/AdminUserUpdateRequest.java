package com.nhnacademy.book2onandonfrontservice.dto.userDto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserUpdateRequest {
    private String role;
    private String status;
    private String gradeName;
}
