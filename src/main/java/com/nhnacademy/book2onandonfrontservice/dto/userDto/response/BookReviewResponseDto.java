package com.nhnacademy.book2onandonfrontservice.dto.userDto.response;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookReviewResponseDto {
    private Long id;
    private Long bookId;
    private Long userId;
    private String title;
    private String content;
    private String score;
    private LocalDate createdAt;
    private List<ReviewImageResponse> images;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ReviewImageResponse {
        private Long id;
        private String imagePath;
    }
}
