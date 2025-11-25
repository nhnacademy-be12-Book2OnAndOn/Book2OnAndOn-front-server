package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

// Gateway를 통해 Book Service와 통신
@FeignClient(name = "gateway-service", contextId = "bookClient", url = "${gateway.base-url}")
public interface BookClient {

    // 1. 메인 도서 목록 (페이징)
    @GetMapping("/api/books")
    Page<BookDto> getBooks(@RequestParam("page") int page, @RequestParam("size") int size);

    // 2. 카테고리 목록 가져오기
    @GetMapping("/api/books/categories")
    List<Map<String, Object>> getCategories();

    // 3. 인기 태그 가져오기
    @GetMapping("/api/books/tags/popular")
    List<String> getPopularTags();

    // 4. 메인 프로모션 도서 가져오기
    @GetMapping("/api/books/promotions/main")
    Map<String, Object> getMainPromotion();

    // 5. 베스트셀러
    @GetMapping("/api/books/bestseller")
    List<BookDto> getBestsellers();

    // 6. 신간
    @GetMapping("/api/books/new")
    List<BookDto> getNewBooks();

    // 7. 검색
    @GetMapping("/api/books/search")
    Page<BookDto> searchBooks(@RequestParam("keyword") String keyword,
                              @RequestParam("page") int page,
                              @RequestParam("size") int size);
}