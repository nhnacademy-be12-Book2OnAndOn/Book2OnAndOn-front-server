package com.nhnacademy.book2onandonfrontservice.dto.orderDto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefundStatusUpdateRequestDto {
    private int statusCode;
}