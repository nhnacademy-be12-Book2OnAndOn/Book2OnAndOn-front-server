package com.nhnacademy.book2onandonfrontservice.dto.bookdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookImageDto {
    private Long id;
    private String url;
    private boolean isThumbnail;
}