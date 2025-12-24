package com.nhnacademy.book2onandonfrontservice.dto.orderDto;// [Front Server] com.nhnacademy.front.dto.GuestLoginRequestDto

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GuestLoginRequestDto {
    private String orderNumber;
    private String guestPassword;
}