package com.nhnacademy.book2onandonfrontservice.dto.bookdto;

public record BookLikeToggleResponse(
        boolean liked,
        long likeCount
) {
}
