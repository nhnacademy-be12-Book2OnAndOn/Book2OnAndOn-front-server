package com.nhnacademy.book2onandonfrontservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDto;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.DashboardDataDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class BookMainServiceTest {

    @Mock
    private BookClient bookClient;

    @InjectMocks
    private BookMainService bookMainService;

    @Test
    @DisplayName("대시보드 데이터 병렬 조회")
    void getDashboardDataParallel_Success() {
        String bearer = "Bearer token";
        int page = 0;
        int size = 5;
        Page<BookDto> mockPage = new PageImpl<>(List.of(mock(BookDto.class)));

        given(bookClient.getNewArrivals(eq(bearer), any(), eq(page), eq(size))).willReturn(mockPage);
        given(bookClient.getBestsellers(eq(bearer), eq("DAILY"), any())).willReturn(mockPage);
        given(bookClient.getBestsellers(eq(bearer), eq("WEEKLY"), any())).willReturn(mockPage);
        given(bookClient.getPopularBooks(eq(bearer), eq(page), eq(size))).willReturn(mockPage);

        DashboardDataDto result = bookMainService.getDashboardDataParallel(bearer, page, size);

        assertThat(result).isNotNull();
        assertThat(result.newBooks().getContent()).hasSize(1);
        assertThat(result.bestDaily().getContent()).hasSize(1);
        assertThat(result.bestWeek().getContent()).hasSize(1);
        assertThat(result.likeBest().getContent()).hasSize(1);
    }

    @Test
    @DisplayName("대시보드 데이터 일부 조회 실패")
    void getDashboardDataParallel_ApiFailure() {
        String bearer = "Bearer token";
        Page<BookDto> mockPage = new PageImpl<>(List.of(mock(BookDto.class)));

        given(bookClient.getNewArrivals(anyString(), any(), anyInt(), anyInt())).willReturn(mockPage);
        given(bookClient.getBestsellers(eq(bearer), eq("DAILY"), any())).willThrow(new RuntimeException("API Error"));
        given(bookClient.getBestsellers(eq(bearer), eq("WEEKLY"), any())).willReturn(mockPage);
        given(bookClient.getPopularBooks(anyString(), anyInt(), anyInt())).willReturn(mockPage);

        DashboardDataDto result = bookMainService.getDashboardDataParallel(bearer, 0, 5);

        assertThat(result.newBooks()).isNotEmpty();
        assertThat(result.bestDaily()).isEmpty();
        assertThat(result.bestWeek()).isNotEmpty();
        assertThat(result.likeBest()).isNotEmpty();
    }

    @Test
    @DisplayName("대시보드 데이터 전체 조회 실패")
    void getDashboardDataParallel_AllFailure() {
        String bearer = "Bearer token";

        given(bookClient.getNewArrivals(anyString(), any(), anyInt(), anyInt())).willThrow(new RuntimeException());
        given(bookClient.getBestsellers(anyString(), anyString(), any())).willThrow(new RuntimeException());
        given(bookClient.getPopularBooks(anyString(), anyInt(), anyInt())).willThrow(new RuntimeException());

        DashboardDataDto result = bookMainService.getDashboardDataParallel(bearer, 0, 5);

        assertThat(result.newBooks()).isEmpty();
        assertThat(result.bestDaily()).isEmpty();
        assertThat(result.bestWeek()).isEmpty();
        assertThat(result.likeBest()).isEmpty();
    }
}
