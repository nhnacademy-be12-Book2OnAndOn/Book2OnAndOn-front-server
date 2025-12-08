package com.nhnacademy.book2onandonfrontservice.dto.bookdto;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookDetailResponse {

    private Long id;    // 도서 아이디
    private String isbn;    // isbn

    private String title;   // 도서 제목
    private String volume;  // 도서 권 제목

    private String contributorName; // 기여자 이름

    private LocalDate publishDate;  // 도서 출판일
    private List<PublisherDto> publishers;  // 출판사

    private Long priceStandard; // 도서 정가
    private Long priceSales; // 도서 판매가

    private BookStatus status; // 책 재고 상태

    private Integer stockCount; // 책 재고 갯수


    private List<CategoryDto> categories;   // 카테고리
    private List<TagDto> tags;  // 태그
    private Boolean isWrapped;  // 포장 여부

    private List<BookImageDto> images;   // 도서 이미지

    private String chapter; // 도서 목차

    private String descriptionHtml; // 도서 설명

    private Long likeCount; // 전체 좋아요 수
    private Boolean likedByCurrentUser; // 사용자가 좋아요를 눌렀는지의 여부

    private List<ReviewDto> reviews;   // 상위 몇 개만
    private Double rating;       // 평균 평점
    private Long reviewCount;          // 전체 리뷰 개수

    private boolean isThumbnail; //프론트에서 별 표시 할 때 필요함

}
