package com.nhnacademy.book2onandonfrontservice.controller.bookController;

import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDetailResponse;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDto;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookSearchCondition;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookStatus;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.CategoryDto;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.DashboardDataDto;
import com.nhnacademy.book2onandonfrontservice.exception.NotFoundBookException;
import com.nhnacademy.book2onandonfrontservice.service.BookMainService;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import com.nhnacademy.book2onandonfrontservice.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import feign.FeignException;
import java.util.Collections;
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
    private final BookMainService bookMainService;

    /// 메인 페이지 (대시보드)
    @GetMapping("/")
    public String dashboard(@RequestParam(defaultValue = "0") int page,
                            HttpServletRequest request,
                            Model model) {
        commonData(model);
        String bearer = toBearer(CookieUtils.getCookieValue(request, "accessToken"));

        DashboardDataDto data = bookMainService.getDashboardDataParallel(bearer, page, DASHBOARD_SECTION_SIZE);
        log.debug("bestsellers: {}", data.bestDaily().getSize());
        model.addAttribute("newBooks", data.newBooks());
        model.addAttribute("bestDaily", data.bestDaily());
        model.addAttribute("bestWeek", data.bestWeek());
        model.addAttribute("likeBest", data.likeBest());

        return "dashboard";
    }

    @GetMapping("/books/bestsellers")
    public String bestsellers(@RequestParam(defaultValue = "WEEKLY") String period,
                              @PageableDefault(size = 20) Pageable pageable, // size 20 추천
                              HttpServletRequest request,
                              Model model) {
        commonData(model);
        String bearer = toBearer(CookieUtils.getCookieValue(request, "accessToken"));

        Page<BookDto> result = Page.empty(pageable);
        try {
            result = bookClient.getBestsellers(bearer, period, pageable);
        } catch (Exception e) {
            log.error("베스트셀러 조회 실패", e);
            model.addAttribute("searchError", "베스트셀러 목록을 불러오지 못했습니다.");
        }

        String pageTitle = "WEEKLY".equalsIgnoreCase(period) ? "주간 베스트셀러" : "일간 베스트셀러";

        model.addAttribute("books", result.getContent());
        model.addAttribute("page", result);
        model.addAttribute("pageTitle", pageTitle);

        // 검색바/필터 UI가 깨지지 않도록 빈 조건 객체 전달
        model.addAttribute("condition", new BookSearchCondition());
        model.addAttribute("searchType", "BESTSELLER");
        model.addAttribute("period", period);

        return "books/search-result";
    }

    /**
     * 신간 도서 전체보기 (페이징 지원)
     * URL: /books/new?page=0
     */
    @GetMapping("/books/new")
    public String newBooks(@PageableDefault(size = 20) Pageable pageable,
                           HttpServletRequest request,
                           Model model) {
        commonData(model);
        String bearer = toBearer(CookieUtils.getCookieValue(request, "accessToken"));

        Page<BookDto> result = Page.empty(pageable);
        try {
            result = bookClient.getNewArrivals(bearer, null, pageable.getPageNumber(), pageable.getPageSize());
        } catch (Exception e) {
            log.error("신간 도서 조회 실패", e);
            model.addAttribute("searchError", "신간 도서 목록을 불러오지 못했습니다.");
        }

        model.addAttribute("books", result.getContent());
        model.addAttribute("page", result);
        model.addAttribute("pageTitle", "신간 도서");
        model.addAttribute("condition", new BookSearchCondition());

        return "books/search-result";
    }

    /// 도서 상세조회
    @GetMapping("/books/{bookId:[0-9]+}")
    public String getBookDetail(@PathVariable Long bookId, HttpServletRequest request, Model model) {
        commonData(model);
        BookDetailResponse bookDetail = bookClient.getBookDetail(bookId);

        if (bookDetail == null || BookStatus.BOOK_DELETED.equals(bookDetail.getStatus())) {
            throw new NotFoundBookException(bookId);
        }

        model.addAttribute("bookDetail", bookDetail);
        String accessToken = CookieUtils.getCookieValue(request, "accessToken");
        boolean canReview = false;
        String reviewEligibilityWarning = null;

        if(accessToken != null){
            try{
                String bearer = toBearer(accessToken);
                Long userId = resolveUserId(accessToken);
                if (userId != null) {
                    canReview = bookClient.checkReviewEligibility(bearer, userId, bookId);
                } else {
                    log.warn("리뷰 권한 체크 실패: userId 추출 불가");
                }
            } catch (FeignException e) {
                int status = e.status();
                // 서버 오류 시에는 작성 버튼은 보여주되 안내 메시지 추가
                if (status >= 500) {
                    canReview = true;
                    reviewEligibilityWarning = "리뷰 작성 가능 여부 확인에 실패했습니다. 작성 시 서버에서 거절될 수 있습니다.";
                    log.warn("리뷰 권한 체크 실패(서버 오류, fallback 허용): status={}, msg={}", status, e.getMessage());
                } else {
                    log.warn("리뷰 권한 체크 실패: status={}, msg={}", status, e.getMessage());
                }
            } catch (Exception e) {
                canReview = true;
                reviewEligibilityWarning = "리뷰 권한 확인 중 오류가 발생했습니다. 작성 시 서버에서 거절될 수 있습니다.";
                log.warn("리뷰 권한 체크 실패(일반 오류, fallback 허용): {}", e.getMessage());

            }
        }

        model.addAttribute("canReview", canReview);
        model.addAttribute("reviewEligibilityWarning", reviewEligibilityWarning);
        return "books/book-detail";
    }

    /// 도서 검색
    @GetMapping("/books/search")
    public String searchBooks(@ModelAttribute BookSearchCondition condition,
                              @PageableDefault(size = 12) Pageable pageable,
                              HttpServletRequest request,
                              Model model) {
        condition.setStatusFilter(Set.of(
                BookStatus.ON_SALE,
                BookStatus.SOLD_OUT,
                BookStatus.OUT_OF_STOCK
        ));
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

    private Long resolveUserId(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return null;
        }
        try {
            return JwtUtils.getUserId(accessToken);
        } catch (Exception e) {
            log.warn("토큰에서 userId 추출 실패: {}", e.getMessage());
            return null;
        }
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
