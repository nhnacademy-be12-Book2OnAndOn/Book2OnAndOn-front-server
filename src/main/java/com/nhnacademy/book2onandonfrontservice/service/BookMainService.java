package com.nhnacademy.book2onandonfrontservice.service;

import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDto;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.DashboardDataDto;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookMainService {

    private final BookClient bookClient;

    public DashboardDataDto getDashboardDataParallel(String bearer, int page, int size) {

        CompletableFuture<Page<BookDto>> newBooksFuture = CompletableFuture.supplyAsync(() ->
                        safeFetchPage(() -> bookClient.getNewArrivals(bearer, null, page, size), "신간"))
                .orTimeout(3, TimeUnit.SECONDS) // 3초 지나면 에러 발생시킴
                .exceptionally(e -> Page.empty()); // 에러 나면 빈 페이지 반환

        CompletableFuture<Page<BookDto>> bestDailyFuture = CompletableFuture.supplyAsync(() ->
                        safeFetchPage(() -> bookClient.getBestsellers(bearer, "DAILY", PageRequest.of(0, size)), "일간 베스트"))
                .orTimeout(3, TimeUnit.SECONDS)
                .exceptionally(e -> Page.empty());

        CompletableFuture<Page<BookDto>> bestWeekFuture = CompletableFuture.supplyAsync(() ->
                        safeFetchPage(() -> bookClient.getBestsellers(bearer, "WEEKLY", PageRequest.of(0, size)), "주간 베스트"))
                .orTimeout(3, TimeUnit.SECONDS)
                .exceptionally(e -> Page.empty());

        CompletableFuture<Page<BookDto>> likeBestFuture = CompletableFuture.supplyAsync(() ->
                        safeFetchPage(() -> bookClient.getPopularBooks(bearer, page, size), "인기 도서"))
                .orTimeout(3, TimeUnit.SECONDS)
                .exceptionally(e -> Page.empty());

        try {
            CompletableFuture.allOf(newBooksFuture, bestDailyFuture, bestWeekFuture, likeBestFuture).join();
        } catch (Exception e) {
            log.error("대시보드 데이터 병렬 조회 중 타임아웃 또는 오류: {}", e.getMessage());
        }

        // 3. 결과 조립 및 반환
        return DashboardDataDto.builder()
                .newBooks(newBooksFuture.getNow(Page.empty())) // getNow: 값이 없으면 빈 페이지
                .bestDaily(bestDailyFuture.getNow(Page.empty()))
                .bestWeek(bestWeekFuture.getNow(Page.empty()))
                .likeBest(likeBestFuture.getNow(Page.empty()))
                .build();
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