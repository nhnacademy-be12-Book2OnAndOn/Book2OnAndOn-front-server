package controller.couponcontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.nhnacademy.book2onandonfrontservice.Book2OnAndOnFrontServiceApplication;
import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.client.CouponClient;
import com.nhnacademy.book2onandonfrontservice.client.CouponPolicyClient;
import com.nhnacademy.book2onandonfrontservice.controller.GlobalControllerAdvice;
import com.nhnacademy.book2onandonfrontservice.controller.couponController.CouponPolicyViewController;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDetailResponse;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookSearchCondition;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.CategoryDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.CouponPolicyDto;
import com.nhnacademy.book2onandonfrontservice.interceptor.AdminInterceptor;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = CouponPolicyViewController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = GlobalControllerAdvice.class
    )
)
@ContextConfiguration(classes = Book2OnAndOnFrontServiceApplication.class)
@WithMockUser(roles = "ADMIN")
class CouponPolicyViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AdminInterceptor adminInterceptor;
    @MockitoBean private CouponPolicyClient couponPolicyClient;
    @MockitoBean private CouponClient couponClient;
    @MockitoBean private BookClient bookClient;

    private final Cookie authCookie = new Cookie("accessToken", "test-token");

    @BeforeEach
    void setUp() throws Exception {
        given(adminInterceptor.preHandle(any(), any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("쿠폰 정책 목록 페이지 조회 성공")
    void listPolicies_View() throws Exception {
        given(couponPolicyClient.getPolicies(anyString(), anyInt(), anyInt(), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/admin/policies").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/couponPolicy/list"))
                .andExpect(model().attributeExists("policies", "startPage", "endPage"));
    }

    @Test
    @DisplayName("정책 등록 폼 조회 성공")
    void createForm_View() throws Exception {
        given(bookClient.getCategories()).willReturn(List.of());

        mockMvc.perform(get("/admin/policies/create").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/couponPolicy/form"))
                .andExpect(model().attributeExists("policy", "categoryList"));
    }

    @Test
    @DisplayName("정책 등록 처리 및 목록 리다이렉트")
    void createPolicy_Process() throws Exception {
        mockMvc.perform(post("/admin/policies/create")
                        .param("couponPolicyName", "신규정책")
                        .cookie(authCookie)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/policies"));
    }

    @Test
    @DisplayName("정책 수정 폼 조회 시 도서 및 카테고리 이름 매핑 확인")
    void updateForm_View() throws Exception {
        // Mock Policy 데이터 설정
        CouponPolicyDto policy = mock(CouponPolicyDto.class);
        given(policy.getTargetBookIds()).willReturn(List.of(1L));
        given(policy.getTargetCategoryIds()).willReturn(List.of(2L));
        given(couponPolicyClient.getPolicy(anyString(), anyLong())).willReturn(policy);

        // Mock Book 및 Category 데이터 설정
        BookDetailResponse book = mock(BookDetailResponse.class);
        given(book.getTitle()).willReturn("테스트 도서");
        given(bookClient.getBookDetail(1L)).willReturn(book);

        CategoryDto category = CategoryDto.builder().id(2L).name("테스트 카테고리").build();
        given(bookClient.getCategories()).willReturn(List.of(category));

        mockMvc.perform(get("/admin/policies/update/1").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/couponPolicy/form"))
                .andExpect(model().attributeExists("bookNameMap", "categoryNameMap"));
    }

    @Test
    @DisplayName("정책 상세 페이지 조회 성공")
    void viewPolicyDetails_View() throws Exception {
        CouponPolicyDto policy = mock(CouponPolicyDto.class);
        given(couponPolicyClient.getPolicy(anyString(), anyLong())).willReturn(policy);

        mockMvc.perform(get("/admin/policies/details/1").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/couponPolicy/detail"))
                .andExpect(model().attributeExists("policy"));
    }

    @Test
    @DisplayName("정책 비활성화(삭제) 처리")
    void deactivatePolicy_Process() throws Exception {
        mockMvc.perform(post("/admin/policies/delete/1")
                        .cookie(authCookie)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/policies"));
    }

    @Test
    @DisplayName("정책 기반 쿠폰 대량 생성 요청")
    void createCoupon_Process() throws Exception {
        mockMvc.perform(post("/admin/policies/1/create-coupon")
                        .param("quantity", "100")
                        .cookie(authCookie)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/policies/details/1"));
    }

    @Test
    @DisplayName("정책 설정을 위한 도서 검색 API 호출")
    void searchBooksForPolicy_Api() throws Exception {
        given(bookClient.searchBooks(any(), any(), any()))
                .willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/admin/policies/books/search")
                        .param("keyword", "검색어")
                        .cookie(authCookie))
                .andExpect(status().isOk());
    }
}