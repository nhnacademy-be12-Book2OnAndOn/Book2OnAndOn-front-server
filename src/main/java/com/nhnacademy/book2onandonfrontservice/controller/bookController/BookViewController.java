package com.nhnacademy.book2onandonfrontservice.controller.bookController;

import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDetailResponse;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDto;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookSearchCondition;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.CategoryDto;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BookViewController {

    private static final int DASHBOARD_SECTION_SIZE = 20;

    private final BookClient bookClient;

    /// 메인 페이지 (대시보드)
    @GetMapping("/")
    public String dashboard(@RequestParam(defaultValue = "0") int page, Model model) {
        commonData(model);
        Page<BookDto> newBooks = bookClient.getNewArrivals(null, page, 0);
        List<BookDto> bestsellerDaily = Collections.emptyList();
        List<BookDto> bestsellerWeek = Collections.emptyList();
        Page<BookDto> likeBest = Page.empty();
        try {
            likeBest = bookClient.getPopularBooks(page, 0);
            log.info("인기도서 갯수: {}", likeBest.getSize());
        } catch (Exception e) {
            log.error("인기 도서 조회 실패", e);
        }

        model.addAttribute("newBooks", newBooks);
        model.addAttribute("bestDaily", cleanBookList(bestsellerDaily));
        model.addAttribute("bestWeek", cleanBookList(bestsellerWeek));
        model.addAttribute("likeBest", likeBest != null ? likeBest : Page.empty());
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
        return "books/book-detail";
    }

    /// 도서 검색
    @GetMapping("/books/search")
    public String searchBooks(@ModelAttribute BookSearchCondition condition,
                              @PageableDefault(size = 12) Pageable pageable,
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

        return "books/search-result";
    }

    /// 카테고리별 조회
    @GetMapping("/books/categories/{categoryId}")
    public String getBooksByCategoryId(@PathVariable Long categoryId, Model model) {
        Page<BookDto> booksByCategory = bookClient.getBooksByCategories(categoryId);
        CategoryDto category = bookClient.getCategoryInfo(categoryId);
        model.addAttribute("books", booksByCategory);
        model.addAttribute("currentCategoryId", categoryId);
        model.addAttribute("categoryName", category.getName());
        return "books/booksByCategory";
    }

    /// 최근 본 도서 (플로팅 패널)
    @GetMapping("/api/books/recent-views")
    @ResponseBody
    public ResponseEntity<List<BookDto>> getRecentViews(HttpServletRequest request) {
        try {
            String accessToken = CookieUtils.getCookieValue(request, "accessToken");
            String guestId = CookieUtils.getCookieValue(request, "GUEST_ID");

            mergeRecentViews(accessToken, guestId);

            if ((accessToken == null || accessToken.isBlank()) && guestId == null) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            List<BookDto> views = bookClient.getRecentViews(toBearer(accessToken), guestId);
            return ResponseEntity.ok(views != null ? views : Collections.emptyList());
        } catch (Exception e) {
            log.error("최근 본 도서 조회 실패", e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    private Page<BookDto> fetchNewArrivals() {
        try {
            return bookClient.getNewArrivals(null, 0, DASHBOARD_SECTION_SIZE);
        } catch (Exception e) {
            log.error("신간 도서 조회 실패", e);
            return Page.empty();
        }
    }

    private List<BookDto> fetchPopular(int page) {
        try {
            Page<BookDto> pageResult = bookClient.getPopularBooks(page, DASHBOARD_SECTION_SIZE);
            return pageResult != null && pageResult.getContent() != null
                    ? cleanBookList(pageResult.getContent())
                    : Collections.emptyList();
        } catch (Exception e) {
            log.error("인기 도서 조회 실패", e);
            return Collections.emptyList();
        }
    }

    private List<BookDto> fetchBestsellers(String period) {
        try {
            return cleanBookList(bookClient.getBestsellers(period));
        } catch (Exception e) {
            log.error("{} 베스트셀러 조회 실패", period, e);
            return Collections.emptyList();
        }
    }

    private List<BookDto> selectBooks(List<BookDto> books, boolean randomize) {
        if (books == null || books.isEmpty()) {
            return Collections.emptyList();
        }
        List<BookDto> working = new ArrayList<>(books);
        if (randomize) {
            Collections.shuffle(working);
        }
        int toIndex = Math.min(DASHBOARD_SECTION_SIZE, working.size());
        return new ArrayList<>(working.subList(0, toIndex));
    }

    private boolean haveSameIds(List<BookDto>... lists) {
        Set<Long> base = null;
        for (List<BookDto> list : lists) {
            Set<Long> ids = extractIds(list);
            if (ids.isEmpty()) {
                return false;
            }
            if (base == null) {
                base = ids;
            } else if (!base.equals(ids)) {
                return false;
            }
        }
        return base != null;
    }

    private Set<Long> extractIds(List<BookDto> books) {
        if (books == null) {
            return Collections.emptySet();
        }
        return books.stream()
                .map(BookDto::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private List<BookDto> mergeLists(List<BookDto>... lists) {
        List<BookDto> merged = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (List<BookDto> list : lists) {
            if (list == null) {
                continue;
            }
            for (BookDto book : list) {
                if (book == null || book.getId() == null) {
                    continue;
                }
                if (seen.add(book.getId())) {
                    merged.add(book);
                }
            }
        }
        return merged;
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
            if (id == null) {
                return null;
            }
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

    // 공통 데이터 (카테고리, 태그 등) 로딩 헬퍼 메서드
    private void commonData(Model model) {
        model.addAttribute("categories", bookClient.getCategories());
//        model.addAttribute("popularTags", bookClient.getPopularTags());
    }


    private Long toLong(Object v) {
        if (v == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double toDouble(Object v) {
        if (v == null) {
            return null;
        }
        try {
            return Double.parseDouble(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String toStr(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private void mergeRecentViews(String accessToken, String guestId) {
        if (guestId == null || accessToken == null || accessToken.isBlank()) {
            return;
        }
        try {
            bookClient.mergeRecentViews(toBearer(accessToken), guestId);
        } catch (Exception e) {
            log.warn("최근 본 도서 병합 실패", e);
        }
    }

    private String toBearer(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return null;
        }
        return accessToken.startsWith("Bearer ") ? accessToken : "Bearer " + accessToken;
    }
}
