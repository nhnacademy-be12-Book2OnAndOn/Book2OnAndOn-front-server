package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDto;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name ="gateway-client", url = "${gateway.base-url}")
public interface BookClient {

    @GetMapping("/api/books")
    List<BookDto> getBooks(); // 예시
}