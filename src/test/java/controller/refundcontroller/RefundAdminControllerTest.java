package controller.refundcontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.nhnacademy.book2onandonfrontservice.client.RefundAdminClient;
import com.nhnacademy.book2onandonfrontservice.controller.refundController.RefundAdminController;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundSearchCondition;
import com.nhnacademy.book2onandonfrontservice.dto.refundDto.RefundStatusUpdateRequestDto;
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

class RefundAdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RefundAdminClient refundAdminClient;

    @InjectMocks
    private RefundAdminController refundAdminController;

    private final Cookie authCookie = new Cookie("accessToken", "test-token");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(refundAdminController)
                .setViewResolvers(viewResolver)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("관리자 반품 목록 조회 성공")
    void list_Success() throws Exception {
        given(refundAdminClient.getRefundList(anyString(), any(RefundSearchCondition.class), any()))
                .willReturn(new PageImpl<>(List.of(new RefundResponseDto()), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/admin/refunds")
                        .cookie(authCookie)
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("refund/admin/list"))
                .andExpect(model().attributeExists("page", "refunds"));
    }

    @Test
    @DisplayName("관리자 반품 상세 조회 성공")
    void detail_Success() throws Exception {
        RefundResponseDto mockDetail = mock(RefundResponseDto.class);
        given(refundAdminClient.findRefundDetails(anyString(), anyLong())).willReturn(mockDetail);

        mockMvc.perform(get("/admin/refunds/1")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("refund/admin/detail"))
                .andExpect(model().attributeExists("detail", "updateRequest"));
    }

    @Test
    @DisplayName("반품 상태 변경 성공 시 상세 페이지 리다이렉트")
    void updateStatus_Success() throws Exception {
        given(refundAdminClient.updateRefundStatus(anyString(), anyLong(), any(RefundStatusUpdateRequestDto.class)))
                .willReturn(null);

        mockMvc.perform(post("/admin/refunds/1/status")
                        .param("status", "1")
                        .cookie(authCookie))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/refunds/1"))
                .andExpect(flash().attribute("message", "반품 상태가 변경되었습니다."));
    }

    @Test
    @DisplayName("반품 상세 조회 실패 시 목록으로 리다이렉트")
    void detail_Fail_Exception() throws Exception {
        given(refundAdminClient.findRefundDetails(anyString(), anyLong())).willThrow(new RuntimeException());

        mockMvc.perform(get("/admin/refunds/1")
                        .cookie(authCookie))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/refunds"))
                .andExpect(flash().attribute("error", "반품 상세를 불러오지 못했습니다."));
    }
}
