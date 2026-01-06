package controller.bookcontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.nhnacademy.book2onandonfrontservice.Book2OnAndOnFrontServiceApplication;
import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.controller.GlobalControllerAdvice;
import com.nhnacademy.book2onandonfrontservice.controller.bookController.BookViewController;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDetailResponse;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDto;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookSearchCondition;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookStatus;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.CategoryDto;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.DashboardDataDto;
import com.nhnacademy.book2onandonfrontservice.interceptor.AdminInterceptor;
import com.nhnacademy.book2onandonfrontservice.service.BookMainService;
import jakarta.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = BookViewController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = GlobalControllerAdvice.class
    )
)
@ContextConfiguration(classes = Book2OnAndOnFrontServiceApplication.class)
@WithMockUser
class BookViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AdminInterceptor adminInterceptor;
    @MockitoBean private BookClient bookClient;
    @MockitoBean private BookMainService bookMainService;

    private final Cookie authCookie = new Cookie("accessToken", "test-token");

    @BeforeEach
    void setUp() throws Exception {
        given(adminInterceptor.preHandle(any(), any(), any())).willReturn(true);
        given(bookClient.getCategories()).willReturn(List.of());
    }

    @Test
    @DisplayName("메인 페이지 대시보드 데이터 조회 및 뷰 반환")
    void dashboard_View() throws Exception {
        Page<BookDto> emptyPage = new PageImpl<>(List.of());
        DashboardDataDto mockData = new DashboardDataDto(emptyPage, emptyPage, emptyPage, emptyPage);
        given(bookMainService.getDashboardDataParallel(any(), anyInt(), anyInt())).willReturn(mockData);

        mockMvc.perform(get("/").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("newBooks", "bestDaily", "bestWeek", "likeBest"));
    }

    @Test
    @DisplayName("베스트셀러 목록 조회 및 검색 결과 뷰 반환")
    void bestsellers_View() throws Exception {
        given(bookClient.getBestsellers(any(), anyString(), any())).willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/books/bestsellers").param("period", "DAILY").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("books/search-result"))
                .andExpect(model().attribute("pageTitle", "일간 베스트셀러"));
    }

    @Test
    @DisplayName("신간 도서 목록 조회 성공")
    void newBooks_View() throws Exception {
        given(bookClient.getNewArrivals(any(), any(), anyInt(), anyInt())).willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/books/new").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("books/search-result"))
                .andExpect(model().attribute("pageTitle", "신간 도서"));
    }

    @Test
    @DisplayName("도서 상세 페이지 조회 성공")
    void getBookDetail_Success() throws Exception {
        BookDetailResponse book = mock(BookDetailResponse.class);
        given(book.getStatus()).willReturn(BookStatus.ON_SALE);
        given(bookClient.getBookDetail(anyLong())).willReturn(book);
        given(bookClient.checkReviewEligibility(anyString(), anyLong(), anyLong())).willReturn(true);

        mockMvc.perform(get("/books/1").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("books/book-detail"))
                .andExpect(model().attribute("canReview", true));
    }

    @Test
    @DisplayName("존재하지 않거나 삭제된 도서 상세 조회 시 404 예외 발생")
    void getBookDetail_NotFound() throws Exception {
        BookDetailResponse book = mock(BookDetailResponse.class);
        given(book.getStatus()).willReturn(BookStatus.BOOK_DELETED);
        given(bookClient.getBookDetail(anyLong())).willReturn(book);

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("도서 검색 결과 페이지 조회")
    void searchBooks_View() throws Exception {
        given(bookClient.searchBooks(any(), any(BookSearchCondition.class), any())).willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/books/search").param("keyword", "test").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("books/search-result"));
    }

    @Test
    @DisplayName("AI 검색 결과 JSON 반환")
    void getAiSearchResult_Success() throws Exception {
        // anyLong() 대신 any()를 사용하여 categoryId가 null인 경우도 대응하게 하거나
        // anyString() 등이 정확히 매칭되도록 합니다.
        given(bookClient.searchAiBooks(any(), anyString(), any())).willReturn("{\"result\":\"ok\"}");

        mockMvc.perform(get("/books/search/ai-result")
                        .param("keyword", "AI추천")
                        // categoryId는 선택사항이므로 안 보내도 되지만,
                        // Mockito stubbing 시 any()를 사용하면 null도 통과됩니다.
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{\"result\":\"ok\"}"));
    }

    @Test
    @DisplayName("카테고리별 도서 목록 조회")
    void getBooksByCategoryId_View() throws Exception {
        given(bookClient.getBooksByCategories(any(), anyLong())).willReturn(new PageImpl<>(List.of()));

        CategoryDto categoryDto = CategoryDto.builder()
                .id(1L)
                .name("소설")
                .parentId(null)
                .children(new ArrayList<>())
                .build();

        given(bookClient.getCategoryInfo(anyLong())).willReturn(categoryDto);

        mockMvc.perform(get("/books/categories/1").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("books/booksByCategory"))
                .andExpect(model().attribute("categoryName", "소설"));
    }

    @Test
    @DisplayName("최근 본 도서 API 호출 및 결과 반환")
    void getRecentViews_Api() throws Exception {
        given(bookClient.getRecentViews(any(), any())).willReturn(List.of(new BookDto()));

        mockMvc.perform(get("/api/books/recent-views")
                        .cookie(new Cookie("GUEST_ID", "guest-123"))
                        .cookie(authCookie))
                .andExpect(status().isOk());
    }
}