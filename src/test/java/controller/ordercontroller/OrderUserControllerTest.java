package controller.ordercontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.book2onandonfrontservice.client.GuestOrderClient;
import com.nhnacademy.book2onandonfrontservice.client.OrderUserClient;
import com.nhnacademy.book2onandonfrontservice.client.RefundClient;
import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.controller.orderController.OrderUserController;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderCreateWrapperRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderPrepareRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderCreateResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderDetailResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderPrepareResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.CurrentPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.service.FrontTokenService;
import jakarta.servlet.http.Cookie;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class OrderUserControllerTest {

    private MockMvc mockMvc;

    @Mock private GuestOrderClient guestOrderClient;
    @Mock private OrderUserClient orderUserClient;
    @Mock private UserClient userClient;
    @Mock private RefundClient refundClient;
    @Mock private FrontTokenService frontTokenService;

    @InjectMocks
    private OrderUserController orderUserController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String ACCESS_TOKEN = "test-token";
    private final String GUEST_ID = "guest-uuid";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderUserController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }
    @Test
    @DisplayName("주문 준비 페이지 이동 - 회원 성공")
    void getOrderPrepare_Member_Success() throws Exception {
        OrderPrepareResponseDto responseDto = new OrderPrepareResponseDto(
                List.of(), List.of(), List.of(), List.of(), new CurrentPointResponseDto(1000));

        given(orderUserClient.getOrderPrepare(anyString(), any(OrderPrepareRequestDto.class)))
                .willReturn(responseDto);

        mockMvc.perform(post("/orders/prepare")
                        .cookie(new Cookie("accessToken", ACCESS_TOKEN))
                        .flashAttr("orderPrepareRequestDto", new OrderPrepareRequestDto(List.of())))
                .andExpect(status().isOk())
                .andExpect(view().name("orderpayment/OrderPayment"))
                .andExpect(model().attribute("isGuest", false));
    }

    @Test
    @DisplayName("주문 준비 페이지 이동 - 비회원 성공")
    void getOrderPrepare_Guest_Success() throws Exception {
        OrderPrepareResponseDto responseDto = new OrderPrepareResponseDto(
                List.of(), List.of(), List.of(), List.of(), new CurrentPointResponseDto(0));
        
        given(guestOrderClient.getOrderPrepare(eq(GUEST_ID), any(OrderPrepareRequestDto.class)))
                .willReturn(responseDto);

        mockMvc.perform(post("/orders/prepare")
                        .cookie(new Cookie("GUEST_ID", GUEST_ID))
                        .flashAttr("orderPrepareRequestDto", new OrderPrepareRequestDto(List.of())))
                .andExpect(status().isOk())
                .andExpect(view().name("orderpayment/OrderPayment"))
                .andExpect(model().attribute("isGuest", true));
    }

    @Test
    @DisplayName("사전 주문 생성 - 회원 성공")
    void createPreOrder_Member_Success() throws Exception {
        OrderCreateWrapperRequestDto wrapper = mock(OrderCreateWrapperRequestDto.class);
        OrderCreateResponseDto response = mock(OrderCreateResponseDto.class);

        given(orderUserClient.createPreOrder(anyString(), any())).willReturn(response);

        mockMvc.perform(post("/orders")
                        .cookie(new Cookie("accessToken", ACCESS_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("내 주문 목록 조회 - 성공")
    void getOrderList_Success() throws Exception {
        Map<String, Object> rawResponse = Map.of(
                "content", List.of(),
                "totalElements", 0
        );
        given(orderUserClient.getOrderList(anyString(), any())).willReturn(rawResponse);

        mockMvc.perform(get("/orders/my-order")
                        .cookie(new Cookie("accessToken", ACCESS_TOKEN)))
                .andExpect(status().isOk())
                .andExpect(view().name("orderpayment/OrderHistory"));
    }

    @Test
    @DisplayName("주문 상세 페이지 뷰 조회 - 성공")
    void orderDetailPage_Success() throws Exception {
        OrderDetailResponseDto orderResponse = mock(OrderDetailResponseDto.class);
        given(orderResponse.getOrderId()).willReturn(1L);
        given(orderUserClient.getOrderDetail(anyString(), isNull(), anyString())).willReturn(orderResponse);
        given(refundClient.getRefundForm(anyString(), isNull(), anyLong())).willReturn(List.of());

        mockMvc.perform(get("/orders/12345/page")
                        .cookie(new Cookie("accessToken", ACCESS_TOKEN)))
                .andExpect(status().isOk())
                .andExpect(view().name("orderpayment/OrderDetail"))
                .andExpect(model().attributeExists("order", "hasRefundableItems"));
    }

    @Test
    @DisplayName("주문 취소 - 성공 (회원)")
    void cancelOrder_Member_Success() throws Exception {
        doNothing().when(orderUserClient).cancelOrder(anyString(), isNull(), eq("12345"));

        mockMvc.perform(get("/orders/12345/cancel")
                        .cookie(new Cookie("accessToken", ACCESS_TOKEN)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/12345/page"));
    }

    @Test
    @DisplayName("주문 취소 - 성공 (비회원)")
    void cancelOrder_Guest_Success() throws Exception {
        doNothing().when(orderUserClient).cancelOrder(isNull(), eq("guest-token"), eq("12345"));

        mockMvc.perform(get("/orders/12345/cancel")
                        .header("X-Guest-Order-Token", "guest-token"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/12345/page"));
    }
}