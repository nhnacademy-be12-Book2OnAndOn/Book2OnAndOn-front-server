package com.nhnacademy.book2onandonfrontservice.service;

import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDto;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.DashboardDataDto;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookMainService {

    private final BookClient bookClient;

    public DashboardDataDto getDashboardDataParallel(String bearer, int page, int size) {

        // 1. 각 API 호출을 비동기로 예약
        CompletableFuture<Page<BookDto>> newBooksFuture = CompletableFuture.supplyAsync(() ->
                safeFetchPage(() -> bookClient.getNewArrivals(bearer, null, page, size), "신간"));

        CompletableFuture<List<BookDto>> bestDailyFuture = CompletableFuture.supplyAsync(() ->
                safeFetchList(() -> bookClient.getBestsellers(bearer, "DAILY"), "일간 베스트"));

        CompletableFuture<List<BookDto>> bestWeekFuture = CompletableFuture.supplyAsync(() ->
                safeFetchList(() -> bookClient.getBestsellers(bearer, "WEEKLY"), "주간 베스트"));

        CompletableFuture<Page<BookDto>> likeBestFuture = CompletableFuture.supplyAsync(() ->
                safeFetchPage(() -> bookClient.getPopularBooks(bearer, page, size), "인기 도서"));

        // 2. 모든 작업이 끝날 때까지 대기
        CompletableFuture.allOf(newBooksFuture, bestDailyFuture, bestWeekFuture, likeBestFuture).join();

        // 3. 결과 조립 및 반환
        return DashboardDataDto.builder()
                .newBooks(newBooksFuture.join())
                .bestDaily(bestDailyFuture.join())
                .bestWeek(bestWeekFuture.join())
                .likeBest(likeBestFuture.join())
                .build();
    }

    // 예외 발생 시 빈 리스트 반환 유틸리티
    private <T> List<T> safeFetchList(Supplier<List<T>> fetcher, String label) {
        try {
            return fetcher.get();
        } catch (Exception e) {
            log.error("{} 조회 중 오류 발생: {}", label, e.getMessage());
            return Collections.emptyList();
        }
    }

    // 예외 발생 시 빈 페이지 반환 유틸리티
    private <T> Page<T> safeFetchPage(Supplier<Page<T>> fetcher, String label) {
        try {
            return fetcher.get();
        } catch (Exception e) {
            log.error("{} 조회 중 오류 발생: {}", label, e.getMessage());
            return Page.empty();
        }
    }
}