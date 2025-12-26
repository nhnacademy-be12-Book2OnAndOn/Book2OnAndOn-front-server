package com.nhnacademy.book2onandonfrontservice.dto.orderDto;// [Front Server] com.nhnacademy.front.dto.GuestLoginRequestDto

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GuestLoginRequestDto {
    private String orderNumber;
    private String guestPassword;
}