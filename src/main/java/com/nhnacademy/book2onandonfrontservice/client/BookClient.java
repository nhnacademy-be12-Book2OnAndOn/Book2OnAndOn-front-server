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
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    /// 카테고리 목록 가져오기
    @GetMapping("/api/books/categories")
    List<CategoryDto> getCategories();

    /// 신간도서목록
    @GetMapping("/api/books/new-arrivals")
    Page<BookDto> getNewArrivals(@RequestParam(value = "categoryId", required = false) Long categoryId,
                                 @RequestParam("page") int page, @RequestParam("size") int size);

    /// 베스트셀러
    @GetMapping("/api/books/bestsellers")
    List<BookDto> getBestsellers(@RequestParam(value = "period", required = false) String period);

    /// 인기도서 조회
    @GetMapping("/api/books/popular")
    Page<BookDto> getPopularBooks(@RequestParam("page") int page, @RequestParam("size") int size);

    /// 도서등록
    @PostMapping(value = "/api/admin/books", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Long createBook(@RequestHeader("Authorization") String accessToken,
                    @RequestPart("book") BookSaveRequest request,
                    @RequestPart(value = "images", required = false) List<MultipartFile> images);

    /// 도서 수정
    @PutMapping(value = "/api/books/{bookId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void updateBook(@RequestHeader("Authorization") String accessToken,
                    @PathVariable("bookId") Long bookId,
                    @RequestPart("book") BookUpdateRequest request,
                    @RequestPart(value = "images", required = false) List<MultipartFile> images);

    /// 도서 썸네일 지정 컨트롤러
    @PutMapping("/api/admin/books/{bookId}/images/{imageId}/thumbnail")
    void updateThumbnail(@PathVariable Long bookId, @PathVariable Long imageId);

    /// 도서 상세 정보
    @GetMapping("/api/books/{bookId}")
    BookDetailResponse getBookDetail(@PathVariable Long bookId);

    /// 도서 삭제
    @DeleteMapping("/api/books/{bookId}")
    void deleteBook(@RequestHeader("Authorization") String accessToken, @PathVariable Long bookId);

    /// 도서 상태 변경
    @PatchMapping("/api/books/{bookId}/status")
    void updateBookStatus(@RequestHeader("Authorization") String accessToken, @PathVariable Long bookId,
                          @RequestBody BookStatusUpdateRequest req);

    /// 도서 최근 본 상품 조회
    @GetMapping("/api/books/recent-views")
    List<BookDto> getRecentViews();

    /// 최근 본 상품 로그인시 병합
    @GetMapping("/api/books/recent-views/merge")
    void mergeRecentViews(@RequestHeader("Authorization") String accessToken,
                          @RequestHeader("X-Guest-Id") String guestId);

    /// 좋아요 토글 요청
    @PostMapping("/api/books/{bookId}/likes")
    BookLikeToggleResponse toggleLike(@RequestHeader("Authorization") String accessToken,
                                      @PathVariable("bookId") Long bookId);

    /// 북 검색엔진
    @GetMapping("/api/books/search")
    Page<BookDto> searchBooks(@SpringQueryMap BookSearchCondition condition,//필드들을 뜯어서 검색조건으로 만듦 즉, 쿼리 파라미터로 만들수 있음
                              @PageableDefault(size = 20) Pageable pageable);
}