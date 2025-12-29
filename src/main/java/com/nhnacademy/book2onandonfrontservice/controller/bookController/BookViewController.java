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
import org.springframework.http.MediaType;
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
    public String dashboard(@RequestParam(defaultValue = "0") int page,
                            HttpServletRequest request,
                            Model model) {
        commonData(model);
        String accessToken = CookieUtils.getCookieValue(request, "accessToken");
        String bearer = toBearer(accessToken);
        Page<BookDto> newBooks = Page.empty();
        try {
            newBooks = bookClient.getNewArrivals(bearer, null, page, DASHBOARD_SECTION_SIZE);
        } catch (Exception e) {
            log.error("신간 도서 조회 실패", e);
        }
        List<BookDto> bestsellerDaily = Collections.emptyList();
        List<BookDto> bestsellerWeek = Collections.emptyList();
        Page<BookDto> likeBest = Page.empty();
        try {
            likeBest = bookClient.getPopularBooks(bearer, page, DASHBOARD_SECTION_SIZE);
//            log.info("인기도서 갯수: {}", likeBest.getSize());
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
                              HttpServletRequest request,
                              Model model) {
        Page<BookDto> result = Page.empty(pageable);
        try {
            String accessToken = CookieUtils.getCookieValue(request, "accessToken");
            result = bookClient.searchBooks(toBearer(accessToken), condition, pageable);
        } catch (Exception e) {
            log.error("도서 검색 실패", e);
            model.addAttribute("searchError", "검색 결과를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.");
        }
        model.addAttribute("books", result.getContent());
        model.addAttribute("page", result);
        model.addAttribute("condition", condition);

        return "books/search-result";
    }

    @GetMapping("/books/search/ai-result")
    @ResponseBody
    public ResponseEntity<String> getAiSearchResult(@RequestParam String keyword,
                                                    @RequestParam(required = false) Long categoryId,
                                                    HttpServletRequest request){
        try{
            String accessToken = CookieUtils.getCookieValue(request, "accessToken");
            String jsonResult = bookClient.searchAiBooks(toBearer(accessToken), keyword, categoryId);

            if(jsonResult==null || jsonResult.isBlank()){
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonResult);
        }catch(Exception e){
            log.warn("AI 검색 결과 조회 실패 (아직 생성 안됨 or 에러) : {}", e.getMessage());
            return ResponseEntity.noContent().build();
        }
    }
    /// 카테고리별 조회
    @GetMapping("/books/categories/{categoryId}")
    public String getBooksByCategoryId(@PathVariable Long categoryId,
                                       HttpServletRequest request,
                                       Model model) {
        commonData(model);
        // 현재 로그인 사용자/카트 카운트 세팅 (헤더 표시용)
        Object sessionUser = request.getSession(false) != null ? request.getSession(false).getAttribute("user") : null;
        model.addAttribute("currentUser", sessionUser);
        Object cartCount = request.getSession(false) != null ? request.getSession(false).getAttribute("cartCount") : null;
        model.addAttribute("cartCount", cartCount);

        String accessToken = CookieUtils.getCookieValue(request, "accessToken");
        Page<BookDto> booksByCategory = Page.empty();
        String categoryName = "카테고리";

        try {
            booksByCategory = bookClient.getBooksByCategories(toBearer(accessToken), categoryId);
        } catch (Exception e) {
            log.error("카테고리 도서 조회 실패: {}", categoryId, e);
            model.addAttribute("categoryError", "카테고리 도서를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            CategoryDto category = bookClient.getCategoryInfo(categoryId);
            if (category != null && category.getName() != null) {
                categoryName = category.getName();
            }
        } catch (Exception e) {
            log.warn("카테고리 정보 조회 실패: {}", categoryId, e);
        }

        model.addAttribute("books", booksByCategory);
        model.addAttribute("currentCategoryId", categoryId);
        model.addAttribute("categoryName", categoryName);
        return "books/booksByCategory";
    }

    /// 최근 본 도서 (플로팅 패널)
    @GetMapping("/api/books/recent-views")
    @ResponseBody
    public ResponseEntity<List<BookDto>> getRecentViews(HttpServletRequest request) {
        try {
            String accessToken = CookieUtils.getCookieValue(request, "accessToken");
            String guestId = resolveGuestId(request);

            mergeRecentViews(request, accessToken, guestId);

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

    private Page<BookDto> fetchNewArrivals(String accessToken) {
        try {
            return bookClient.getNewArrivals(toBearer(accessToken), null, 0, DASHBOARD_SECTION_SIZE);
        } catch (Exception e) {
            log.error("신간 도서 조회 실패", e);
            return Page.empty();
        }
    }

    private List<BookDto> fetchPopular(String accessToken, int page) {
        try {
            Page<BookDto> pageResult = bookClient.getPopularBooks(toBearer(accessToken), page, DASHBOARD_SECTION_SIZE);
            return pageResult != null && pageResult.getContent() != null
                    ? cleanBookList(pageResult.getContent())
                    : Collections.emptyList();
        } catch (Exception e) {
            log.error("인기 도서 조회 실패", e);
            return Collections.emptyList();
        }
    }

    private List<BookDto> fetchBestsellers(String accessToken, String period) {
        try {
            return cleanBookList(bookClient.getBestsellers(toBearer(accessToken), period));
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
        try {
            model.addAttribute("categories", bookClient.getCategories());
            // model.addAttribute("popularTags", bookClient.getPopularTags());
        } catch (Exception e) {
            log.warn("카테고리 조회 실패", e);
            model.addAttribute("categories", Collections.emptyList());
        }
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

    private String resolveGuestId(HttpServletRequest request) {
        String gid = CookieUtils.getCookieValue(request, "GUEST_ID");
        if (gid == null) {
            gid = CookieUtils.getCookieValue(request, "guestId"); // 하위 호환
        }
        return gid;
    }

    private void mergeRecentViews(HttpServletRequest request, String accessToken, String guestId) {
        if (guestId == null || accessToken == null || accessToken.isBlank()) {
            return;
        }
        if (request.getSession().getAttribute("recentViewsMergeAttempted") != null) {
            return;
        }
        try {
            bookClient.mergeRecentViews(toBearer(accessToken), guestId);
        } catch (Exception e) {
            log.warn("최근 본 도서 병합 실패", e);
        } finally {
            request.getSession().setAttribute("recentViewsMergeAttempted", Boolean.TRUE);
        }
    }

    private String toBearer(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return null;
        }
        return accessToken.startsWith("Bearer ") ? accessToken : "Bearer " + accessToken;
    }
}
