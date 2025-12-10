package com.nhnacademy.book2onandonfrontservice.dto.userDto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaycoLoginRequest {
    private String code;
}