package com.nhnacademy.book2onandonfrontservice.dto.bookdto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyLikedBookResponseDto {
    private Long bookLikeId;
    private LocalDateTime createdAt;
    private BookDto bookInfo; // 기존 BookDto 재사용
    private boolean isLiked;

}