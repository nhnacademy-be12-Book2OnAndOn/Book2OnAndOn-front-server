package com.nhnacademy.book2onandonfrontservice.dto.cartDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemCountResponseDto {

    private int itemCount;     // 서로 다른 bookId 개수
    private int totalQuantity; // quantity 합
}
