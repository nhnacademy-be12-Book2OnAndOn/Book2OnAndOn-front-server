package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDto;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.CategoryDto;
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
    List<CategoryDto> getCategories();

}