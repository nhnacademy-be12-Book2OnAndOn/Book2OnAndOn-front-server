
package com.nhnacademy.book2onandonfrontservice.dto.bookdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.domain.Page;

/**
 * @param newBooks  신간 도서
 * @param bestDaily 일간 베스트
 * @param bestWeek  주간 베스트
 * @param likeBest  인기 도서 (좋아요 순)
 */
@Builder
public record DashboardDataDto(Page<BookDto> newBooks, Page<BookDto> bestDaily, Page<BookDto> bestWeek,
                               Page<BookDto> likeBest) {
}