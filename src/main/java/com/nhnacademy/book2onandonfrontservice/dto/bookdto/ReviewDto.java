package com.nhnacademy.book2onandonfrontservice.dto.bookdto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 리뷰 응답
@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
public class ReviewDto {
    private Long id;    // 리뷰 아이디
    private Long bookId;    // 도서 아이디
    private Long userId;    // 회원 아이디
    private String writerName;

    private String title;   // 리뷰 제목
    private String content; // 리뷰 내용
    private Integer score;  // 평가 점수
    private LocalDate writerDate;    // 리뷰 생성 일시


    private List<ReviewImageDto> images;    // 이미지 경로 저장 리스트
}