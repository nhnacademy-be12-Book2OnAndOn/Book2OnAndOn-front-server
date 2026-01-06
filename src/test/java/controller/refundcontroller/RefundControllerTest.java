package controller.refundcontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.nhnacademy.book2onandonfrontservice.client.RefundClient;
import com.nhnacademy.book2onandonfrontservice.controller.refundController.RefundController;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundAvailableItemResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundResponseDto;
import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

class RefundControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RefundClient refundClient;

    @InjectMocks
    private RefundController refundController;

    private final Cookie authCookie = new Cookie("accessToken", "test-member-token");
    private final Cookie guestCookie = new Cookie("guestOrderToken", "test-guest-token");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(refundController)
                .setViewResolvers(viewResolver)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("회원: 내 반품 목록 조회 성공")
    void myRefunds_Success() throws Exception {
        given(refundClient.getMyRefunds(anyString(), any()))
                .willReturn(new PageImpl<>(List.of(new RefundResponseDto()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/users/me/refunds").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("refund/refund-mypage"))
                .andExpect(model().attributeExists("refunds", "refundReasonLabels"));
    }

    @Test
    @DisplayName("반품 신청 폼 조회 성공 - 회원 케이스")
    void refundForm_Member_Success() throws Exception {
        RefundAvailableItemResponseDto mockItem = new RefundAvailableItemResponseDto(
                1L, 101L, "테스트 도서", 2, 0, 2, false, true, 15000
        );

        given(refundClient.getRefundForm(anyString(), isNull(), anyLong()))
                .willReturn(List.of(mockItem));

        mockMvc.perform(get("/orders/1/refunds/form").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("refund/refund-form"))
                .andExpect(model().attribute("userType", "member"));
    }

    @Test
    @DisplayName("인증 정보가 없는 경우 로그인 페이지로 리다이렉트")
    void refundForm_NoToken_Redirect() throws Exception {
        mockMvc.perform(get("/orders/1/refunds/form"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("반품 신청 처리 성공 및 상세 페이지 리다이렉트")
    void submitRefund_Success() throws Exception {
        RefundResponseDto created = mock(RefundResponseDto.class);
        given(created.getRefundId()).willReturn(55L);
        
        given(refundClient.createRefund(anyString(), isNull(), anyLong(), any(RefundRequestDto.class)))
                .willReturn(created);

        mockMvc.perform(post("/orders/1/refunds")
                        .cookie(authCookie)
                        .param("refundReason", "PRODUCT_DEFECT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/1/refunds/55"))
                .andExpect(flash().attribute("message", "반품 신청이 접수되었습니다."));
    }

    @Test
    @DisplayName("반품 상세 정보 조회 성공")
    void refundDetail_Success() throws Exception {
        RefundResponseDto detail = mock(RefundResponseDto.class);
        given(detail.getRefundId()).willReturn(55L);

        given(refundClient.getRefundDetails(anyString(), isNull(), anyLong(), anyLong()))
                .willReturn(detail);

        mockMvc.perform(get("/orders/1/refunds/55").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("refund/refund-detail"))
                .andExpect(model().attributeExists("refund", "refundReasonLabels"));
    }

    @Test
    @DisplayName("반품 취소 처리 성공")
    void cancelRefund_Success() throws Exception {
        // void가 아닐 경우를 대비해 willReturn(null)
        given(refundClient.cancelRefund(anyString(), isNull(), anyLong(), anyLong()))
                .willReturn(null);

        mockMvc.perform(post("/orders/1/refunds/55/cancel").cookie(authCookie))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/1/refunds/55"))
                .andExpect(flash().attribute("message", "반품 신청이 취소되었습니다."));
    }
}