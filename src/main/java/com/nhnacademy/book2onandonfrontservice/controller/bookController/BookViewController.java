package com.nhnacademy.book2onandonfrontservice.controller.bookController;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDetailResponse;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDto;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookSearchCondition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BookViewController {

    private final BookClient bookClient;

    /// 메인 페이지 (대시보드)
    @GetMapping("/")
    public String dashboard(@RequestParam(defaultValue = "0") int page, Model model) {
        commonData(model);
        Page<BookDto> response = null;
        try {
            response = bookClient.getNewArrivals(null, 0, 12);
        } catch (Exception e) {
            log.error("신간 도서 조회 실패", e);
        }
        List<BookDto> bestsellerDaily = Collections.emptyList();
        List<BookDto> bestsellerWeek = Collections.emptyList();
        Page<BookDto> likeBest = null;
        try {
            likeBest = CompletableFuture
                    .supplyAsync(() -> bookClient.getPopularBooks(page, 20))
                    .get(2, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            log.error("인기 도서 조회 타임아웃", te);
        } catch (Exception e) {
            log.error("인기 도서 조회 실패", e);
        }

        List<BookDto> newBooks = response != null && response.getContent() != null
                ? cleanBookList(response.getContent())
                : Collections.emptyList();

        model.addAttribute("newBooks", newBooks);
        model.addAttribute("newBooksPage", response);
        model.addAttribute("bestDaily", cleanBookList(bestsellerDaily));
        model.addAttribute("bestWeek", cleanBookList(bestsellerWeek));
        model.addAttribute("likeBest", likeBest != null && likeBest.getContent() != null ? cleanBookList(likeBest.getContent()) : Collections.emptyList());
        return "dashboard";
    }

    /// 도서 상세조회
    @GetMapping("/books/{bookId:[0-9]+}")
    public String getBookDetail(@PathVariable Long bookId, Model model) {
        commonData(model);
        BookDetailResponse bookDetail = bookClient.getBookDetail(bookId);
        if (bookDetail != null) {
            model.addAttribute("bookDetail", bookDetail);
        }
        //TODO: 북 상세조회 페이지 만들기
        return "books/book-detail";
    }


    // 공통 데이터 (카테고리, 태그 등) 로딩 헬퍼 메서드
    private void commonData(Model model) {
        model.addAttribute("categories", bookClient.getCategories());
//        model.addAttribute("popularTags", bookClient.getPopularTags());
    }

    @GetMapping("/books/search")
    public String searchBooks(@ModelAttribute BookSearchCondition condition,
                              @PageableDefault(size = 20) Pageable pageable,
                              Model model) {
        Page<BookDto> result = Page.empty(pageable);
        try {
            result = bookClient.searchBooks(condition, pageable);
        } catch (Exception e) {
            log.error("도서 검색 실패", e);
            model.addAttribute("searchError", "검색 결과를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.");
        }
        model.addAttribute("books", result.getContent());
        model.addAttribute("page", result);
        model.addAttribute("condition", condition);

        /**
         * <form action="/books/search" method="get" class="search-form">
         *     <input type="text" name="keyword" placeholder="책 제목, 저자, ISBN 검색..." required>
         *
         *     <button type="submit">검색</button>
         * </form>
         */
        //TODO: 밍서가 작성하시오.
        return "books/search-result";
    }

    private List<BookDto> cleanBookList(List<?> books) {
        if (books == null || books.isEmpty()) {
            return Collections.emptyList();
        }
        return books.stream()
                .map(this::toBookDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private BookDto toBookDto(Object obj) {
        if (obj instanceof BookDto dto) {
            return dto.getId() != null ? dto : null;
        }
        if (obj instanceof Map<?, ?> map) {
            Object id = map.get("id");
            if (id == null) return null;
            BookDto dto = new BookDto();
            dto.setId(toLong(id));
            dto.setTitle(toStr(map.get("title")));
            dto.setThumbnail(toStr(map.get("thumbnail")));
            dto.setPriceSales(toLong(map.get("priceSales")));
            dto.setRating(toDouble(map.get("rating")));

            Object contributors = map.get("contributorNames");
            if (contributors instanceof List<?>) {
                dto.setContributorNames(((List<?>) contributors).stream()
                        .filter(Objects::nonNull)
                        .map(String::valueOf)
                        .collect(Collectors.toList()));
            }
            Object likeCount = map.get("likeCount");
            if (likeCount != null) {
                dto.setLikeCount(toLong(likeCount));
            }
            Object likedBy = map.get("likedByCurrentUser");
            if (likedBy instanceof Boolean b) {
                dto.setLikedByCurrentUser(b);
            }
            return dto;
        }
        return null;
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double toDouble(Object v) {
        if (v == null) return null;
        try {
            return Double.parseDouble(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String toStr(Object v) {
        return v == null ? null : String.valueOf(v);
    }
}
