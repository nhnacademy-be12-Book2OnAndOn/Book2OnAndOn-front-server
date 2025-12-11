package com.nhnacademy.book2onandonfrontservice.dto.bookdto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {
    private Long id;    // book_id
    private String title; // 도서 제목
    private String volume;  // 도서 권 제목

    private Long priceStandard; // 도서 정가
    private Long priceSales; // 도서 판매가
    private Double rating; // 평점

    private LocalDate publisherDate; //출간일

    private List<String> contributorNames;  // 기여자 정보
    private List<String> publisherNames;    // 출판사
    private List<String> categoryNames;   // 카테고리
    private List<String> tagNames; // 태그
    private String thumbnail;

    // 좋아요 UI용
    private Boolean likedByCurrentUser = false;
    private Long likeCount = 0L;
}
