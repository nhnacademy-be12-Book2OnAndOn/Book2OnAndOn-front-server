package com.nhnacademy.book2onandonfrontservice.controller.adminController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.nhnacademy.book2onandonfrontservice.Book2OnAndOnFrontServiceApplication;
import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.client.BookReindexClient;
import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.controller.GlobalControllerAdvice;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDetailResponse;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookSaveRequest;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookSearchCondition;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookStatus;
import com.nhnacademy.book2onandonfrontservice.interceptor.AdminInterceptor;
import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        value = BookAdminController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalControllerAdvice.class
        )
)
@ContextConfiguration(classes = Book2OnAndOnFrontServiceApplication.class)
@WithMockUser(roles = "ADMIN")
class BookAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AdminInterceptor adminInterceptor;
    @MockitoBean private BookClient bookClient;
    @MockitoBean private BookReindexClient bookReindexClient;
    @MockitoBean private UserClient userClient; // 컨텍스트 에러 방지용 추가

    private final Cookie authCookie = new Cookie("accessToken", "test-token");

    @BeforeEach
    void setUp() throws Exception {
        given(adminInterceptor.preHandle(any(), any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("도서 등록 요청 처리 및 목록으로 리다이렉트")
    void createBook_Process() throws Exception {
        MockMultipartFile image = new MockMultipartFile("images", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test".getBytes());

        mockMvc.perform(multipart("/admin/books/create")
                        .file(image)
                        .param("title", "Test Book")
                        .cookie(authCookie)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/books"));
    }

    @Test
    @DisplayName("도서 등록 폼 페이지 조회")
    void bookCreateForm_View() throws Exception {
        given(bookClient.getCategories()).willReturn(List.of());

        mockMvc.perform(get("/admin/books/create").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/books/create"))
                .andExpect(model().attributeExists("categories", "statuses"));
    }

    @Test
    @DisplayName("도서 목록 및 검색 결과 페이지 조회")
    void listBooks_View() throws Exception {
        PageRequest pageable = PageRequest.of(0, 10);
        given(bookClient.searchBooks(any(), any(BookSearchCondition.class), any()))
                .willReturn(new PageImpl<>(List.of(), pageable, 0L));
        given(bookClient.getCategories()).willReturn(List.of());

        mockMvc.perform(get("/admin/books").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/books/list"))
                .andExpect(model().attributeExists("page", "books", "condition"));
    }

    @Test
    @DisplayName("전체 도서 재인덱싱 요청")
    void reindexAll_Process() throws Exception {
        mockMvc.perform(post("/admin/books/reindex/all")
                        .cookie(authCookie)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/books"));
    }

    @Test
    @DisplayName("도서 수정 페이지 데이터 조회")
    void editBook_View() throws Exception {
        BookDetailResponse book = mock(BookDetailResponse.class);
        given(book.getTags()).willReturn(List.of());
        given(book.getCategories()).willReturn(List.of());

        given(bookClient.getBookDetail(anyLong())).willReturn(book);
        given(bookClient.getCategories()).willReturn(List.of());

        mockMvc.perform(get("/admin/books/1/edit").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/books/edit"));
    }

    @Test
    @DisplayName("ISBN을 통한 도서 정보 검색 API 호출")
    void searchBookInfo_Api() throws Exception {
        given(bookClient.lookupBook(anyString(), anyString())).willReturn(new BookSaveRequest());

        mockMvc.perform(get("/admin/books/search")
                        .param("isbn", "12345")
                        .cookie(authCookie))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("도서 정보 업데이트 처리")
    void updateBook_Process() throws Exception {
        MockMultipartFile image = new MockMultipartFile("images", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test".getBytes());

        mockMvc.perform(multipart(HttpMethod.PUT, "/admin/books/1")
                        .file(image)
                        .param("title", "Updated Title")
                        .cookie(authCookie)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/books"));
    }

    @Test
    @DisplayName("도서 삭제 요청 처리")
    void deleteBook_Process() throws Exception {
        mockMvc.perform(delete("/admin/books/1")
                        .cookie(authCookie)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/books"));

        verify(bookClient).deleteBook(anyString(), eq(1L));
    }

    @Test
    @DisplayName("도서 판매 상태 변경 처리")
    void updateStatus_Process() throws Exception {
        String statusValue = BookStatus.ON_SALE.name();

        mockMvc.perform(patch("/admin/books/1/status")
                        .param("status", statusValue)
                        .cookie(authCookie)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/books"));
    }
}