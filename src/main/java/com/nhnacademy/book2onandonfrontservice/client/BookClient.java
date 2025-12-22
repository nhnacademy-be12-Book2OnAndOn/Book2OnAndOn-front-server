package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.config.FeignMultipartConfig;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDetailResponse;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDto;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookLikeToggleResponse;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookSaveRequest;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookSearchCondition;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookStatusUpdateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookUpdateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.CategoryDto;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.ReviewCreateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.ReviewUpdateRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

// Gateway를 통해 Book Service와 통신
@FeignClient(name = "gateway-service", contextId = "bookClient", url = "${gateway.base-url}", configuration = FeignMultipartConfig.class)
public interface BookClient {

    /// 카테고리 전체 목록 가져오기 (카테고리 이름들만)
    @GetMapping("/api/categories")
    List<CategoryDto> getCategories();

    /// 카테고리별 도서 목록
    @GetMapping("/api/categories/{categoryId}")
    Page<BookDto> getBooksByCategories(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                       @PathVariable Long categoryId);

    /// 카테고리 이름 반환
    @GetMapping("/api/categories/{categoryId}/info")
    CategoryDto getCategoryInfo(@PathVariable("categoryId") Long categoryId);

    /// 신간도서목록
    @GetMapping("/api/books/new-arrivals")
    Page<BookDto> getNewArrivals(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                 @RequestParam(value = "categoryId", required = false) Long categoryId,
                                 @RequestParam("page") int page, @RequestParam("size") int size);

    /// 베스트셀러
    @GetMapping("/api/books/bestsellers")
    List<BookDto> getBestsellers(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                 @RequestParam(value = "period", required = false) String period);

    /// 인기도서 조회
    @GetMapping("/api/books/popular")
    Page<BookDto> getPopularBooks(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                  @RequestParam("page") int page, @RequestParam("size") int size);

    /// 도서 등록 GoogleBooksApi
    @GetMapping("/api/admin/books/lookup")
    BookSaveRequest lookupBook(@RequestHeader("Authorization") String accessToken,
                               @RequestParam String isbn);

    /// 도서등록
    @PostMapping(value = "/api/admin/books", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Long createBook(@RequestHeader("Authorization") String accessToken,
                    @RequestPart("book") BookSaveRequest request,
                    @RequestPart(value = "images", required = false) List<MultipartFile> images);

    /// 도서 수정
    @PutMapping(value = "/api/admin/books/{bookId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    void updateBook(@RequestHeader("Authorization") String accessToken,
                    @PathVariable("bookId") Long bookId,
                    @RequestPart("book") BookUpdateRequest request,
                    @RequestPart(value = "images", required = false) List<MultipartFile> images);

    /// 도서 썸네일 지정 컨트롤러
    @PutMapping("/api/admin/books/{bookId}/images/{imageId}/thumbnail")
    void updateThumbnail(@RequestHeader("Authorization") String accessToken, @PathVariable Long bookId,
                         @PathVariable Long imageId);

    /// 도서 상세 정보
    @GetMapping("/api/books/{bookId}")
    BookDetailResponse getBookDetail(@PathVariable Long bookId);

    /// 도서 삭제
    @DeleteMapping("/api/books/{bookId}")
    void deleteBook(@RequestHeader("Authorization") String accessToken, @PathVariable Long bookId);

    /// 전체 도서 개수
    @GetMapping("/api/admin/books/total-count")
    Long countAllBook(@RequestHeader("Authorization") String accessToken);

    /// 도서 상태 변경
    @PatchMapping("/api/books/{bookId}/status")
    void updateBookStatus(@RequestHeader("Authorization") String accessToken, @PathVariable Long bookId,
                          @RequestBody BookStatusUpdateRequest req);

    /// 도서 최근 본 상품 조회
    @GetMapping("/api/books/recent-views")
    List<BookDto> getRecentViews(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestHeader(value = "X-Guest-Id", required = false) String guestId);

    /// 최근 본 상품 로그인시 병합
    @PostMapping("/api/books/recent-views/merge")
    void mergeRecentViews(@RequestHeader("Authorization") String accessToken,
                          @RequestHeader("X-Guest-Id") String guestId);

    /// 좋아요 토글 요청
    @PostMapping("/api/books/{bookId}/likes")
    BookLikeToggleResponse toggleLike(@RequestHeader("Authorization") String accessToken,
                                      @RequestHeader("X-USER-ID") Long userId,
                                      @PathVariable("bookId") Long bookId);

    /// --------------- elastic search ----------------- 북 검색엔진
    @PostMapping("/api/books/search")
    Page<BookDto> searchBooks(@RequestHeader(value = "Authorization", required = false) String accessToken,
                              @SpringQueryMap BookSearchCondition condition,//필드들을 뜯어서 검색조건으로 만듦 즉, 쿼리 파라미터로 만들수 있음
                              @SpringQueryMap Pageable pageable);


    /// ----------------- Review ------------------- 리뷰생성
    @PostMapping(value = "/api/books/{bookId}/reivews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Long createReview(@RequestHeader("Authorization") String accessToken,
                      @PathVariable("bookId") Long bookId,
                      @RequestPart("request") ReviewCreateRequest request,
                      @RequestPart(value = "images", required = false) List<MultipartFile> images);

    /// 리뷰 수정
    @PutMapping(value = "/api/books/reviews/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    void updateReview(@RequestHeader("Authorization") String accessToken,
                      @PathVariable("reviewId") Long reviewId,
                      @RequestPart(value = "request") @Valid ReviewUpdateRequest request,
                      @RequestPart(value = "images", required = false) List<MultipartFile> newImages);

    /// --------------할인율 변경 및 가격 상태조회--------------- 할인율 변경 요청 (비동기 실행됨)
    @PostMapping("/api/admin/price/discount")
    String updateDiscountRate(@RequestHeader("Authorization") String accessToken, @RequestParam("rate") int rate);

    /**
     * 가격 상태조회 현재 상시 할인율이 변경되고 있는지 db가 전부 리프레쉬 됐는지 확인하는 버튼? 아님 알림 창
     */
    @GetMapping("/api/admin/price/status")
    String getUpdateStatus(@RequestHeader("Authorization") String accessToken);

}
