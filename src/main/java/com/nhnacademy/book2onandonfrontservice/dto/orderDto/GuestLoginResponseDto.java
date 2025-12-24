package com.nhnacademy.book2onandonfrontservice.dto.orderDto;// [Front Server] com.nhnacademy.front.dto.GuestLoginResponseDto

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GuestLoginResponseDto {
    private String accessToken;
    private Long orderId;
}