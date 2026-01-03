
package com.nhnacademy.book2onandonfrontservice.dto.bookdto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor
@Builder
public class DashboardDataDto {
    private final Page<BookDto> newBooks;    // 신간 도서
    private final List<BookDto> bestDaily;  // 일간 베스트
    private final List<BookDto> bestWeek;   // 주간 베스트
    private final Page<BookDto> likeBest;   // 인기 도서 (좋아요 순)
}