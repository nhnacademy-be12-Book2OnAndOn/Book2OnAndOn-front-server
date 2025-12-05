package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDto;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookSaveRequest;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookUpdateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.CategoryDto;
import org.hibernate.validator.constraints.pl.REGON;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

// Gateway를 통해 Book Service와 통신
@FeignClient(name = "gateway-service", contextId = "bookClient", url = "${gateway.base-url}")
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
    List<BookDto> getBestsellers(@RequestParam(value="period", required = false)String period);

    /// 인기도서 조회
    @GetMapping("/api/books/popular")
    Page<BookDto> getPopularBooks(@RequestParam("page") int page, @RequestParam("size") int size);

    ///도서등록
    @PostMapping(value = "/api/books", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Long createBook(@RequestParam("book") BookSaveRequest request,
                    @RequestParam(value="image", required = false) List<MultipartFile> image);

    /// 도서 수정
    @PutMapping(value="/api/books/{bookId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void updateBook(@PathVariable("bookId") Long bookId,
                @RequestParam("book") BookUpdateRequest request,
                    @RequestPart(value="image",required = false) List<MultipartFile> image);
}