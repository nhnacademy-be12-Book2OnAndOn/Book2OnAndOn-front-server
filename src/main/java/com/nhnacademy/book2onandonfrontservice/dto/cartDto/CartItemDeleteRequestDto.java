package com.nhnacademy.book2onandonfrontservice.dto.cartDto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDeleteRequestDto {

    @NotNull
    private Long bookId;
}

