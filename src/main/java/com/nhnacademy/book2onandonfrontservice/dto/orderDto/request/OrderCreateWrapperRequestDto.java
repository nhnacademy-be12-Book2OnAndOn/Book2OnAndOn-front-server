package com.nhnacademy.book2onandonfrontservice.dto.orderDto.request;

import com.nhnacademy.book2onandonfrontservice.dto.orderDto.guest.GuestOrderCreateRequestDto;

public record OrderCreateWrapperRequestDto(
        OrderCreateRequestDto user,
        GuestOrderCreateRequestDto guest
) {
}
