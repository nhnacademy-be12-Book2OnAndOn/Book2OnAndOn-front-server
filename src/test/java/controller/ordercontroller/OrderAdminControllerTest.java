package com.nhnacademy.book2onandonfrontservice.controller.orderController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.book2onandonfrontservice.client.OrderAdminClient;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderItemStatusUpdateDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderStatusUpdateDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderDetailResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.PageResponse;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.status.OrderItemStatus;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.status.OrderStatus;
import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class OrderAdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderAdminClient orderAdminClient;

    @InjectMocks
    private OrderAdminController orderAdminController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String ADMIN_TOKEN = "admin-token";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderAdminController).build();
    }

    @Test
    @DisplayName("관리자 주문 목록 조회")
    void getOrderList_Success() throws Exception {
        PageResponse response = new PageResponse(List.of(), 0, 10, 0, 0L, true);
        given(orderAdminClient.getOrderList(any())).willReturn(response);

        mockMvc.perform(get("/admin/orders")
                        .cookie(new Cookie("accessToken", ADMIN_TOKEN)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/orders"))
                .andExpect(model().attributeExists("orderList", "currentPage"));
    }

    @Test
    @DisplayName("토큰 없을 때 로그인 페이지로 리다이렉트")
    void getOrderList_NoToken() throws Exception {
        mockMvc.perform(get("/admin/orders"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("주문 상세 페이지 조회")
    void getOrderDetail_Success() throws Exception {
        OrderDetailResponseDto response = mock(OrderDetailResponseDto.class);
        given(orderAdminClient.getOrderDetail(anyString())).willReturn(response);

        mockMvc.perform(get("/admin/orders/12345")
                        .cookie(new Cookie("accessToken", ADMIN_TOKEN)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/order/order-detail"))
                .andExpect(model().attributeExists("order", "orderStatusList"));
    }

    @Test
    @DisplayName("주문 상태 변경 성공")
    void updateOrderStatus_Success() throws Exception {
        OrderStatusUpdateDto request = new OrderStatusUpdateDto(OrderStatus.COMPLETED);

        mockMvc.perform(patch("/admin/orders/12345")
                        .cookie(new Cookie("accessToken", ADMIN_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("주문 상품 상태 변경 성공")
    void updateOrderItemStatus_Success() throws Exception {
        OrderItemStatusUpdateDto request = new OrderItemStatusUpdateDto(1L, OrderItemStatus.SHIPPED);

        mockMvc.perform(patch("/admin/orders/12345/order-items")
                        .cookie(new Cookie("accessToken", ADMIN_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("관리자 주문 취소 처리 및 리다이렉트")
    void cancelOrder_Success() throws Exception {
        doNothing().when(orderAdminClient).cancelOrder(anyString());

        mockMvc.perform(get("/admin/orders/12345/cancel")
                        .cookie(new Cookie("accessToken", ADMIN_TOKEN)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/orders/12345"));
    }

    @Test
    @DisplayName("토큰 없을 때 상세 조회 리다이렉트")
    void getOrderDetail_NoToken() throws Exception {
        mockMvc.perform(get("/admin/orders/12345"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
