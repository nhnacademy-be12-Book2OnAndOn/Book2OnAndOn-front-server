package controller.ordercontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.book2onandonfrontservice.client.GuestOrderClient;
import com.nhnacademy.book2onandonfrontservice.client.OrderUserClient;
import com.nhnacademy.book2onandonfrontservice.controller.orderController.OrderRedirectController;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.GuestLoginRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.GuestLoginResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.guest.GuestOrderCreateRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderPrepareRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderCreateResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderPrepareResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.CurrentPointResponseDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class OrderRedirectControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GuestOrderClient guestOrderClient;

    @Mock
    private OrderUserClient orderUserClient;

    @InjectMocks
    private OrderRedirectController orderRedirectController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderRedirectController).build();
    }

    @Test
    @DisplayName("레거시 주문 내역 경로 리다이렉트")
    void redirectLegacyOrders_Success() throws Exception {
        mockMvc.perform(get("/users/me/orders/view"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/history"));
    }

    @Test
    @DisplayName("비회원 로그인 화면 조회")
    void guestOrderLookup_Success() throws Exception {
        mockMvc.perform(get("/orders/guest/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("orderpayment/OrderHistoryGuest"));
    }

    @Test
    @DisplayName("비회원 로그인 처리 - 성공 및 쿠키 생성")
    void loginProcess_Success() throws Exception {
        GuestLoginRequestDto requestDto = new GuestLoginRequestDto("order-123", "password");
        GuestLoginResponseDto responseBody = new GuestLoginResponseDto("guest-token", "ORD-123");
        
        given(guestOrderClient.loginGuest(any(GuestLoginRequestDto.class)))
                .willReturn(ResponseEntity.ok(responseBody));

        mockMvc.perform(post("/orders/guest/login")
                        .param("orderNumber", "order-123")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(cookie().value("guestOrderToken", "guest-token"))
                .andExpect(cookie().path("guestOrderToken", "/"))
                .andExpect(redirectedUrl("/guest/orders/ORD-123"));
    }

    @Test
    @DisplayName("비회원 로그인 처리 - 실패 시 에러 파라미터와 함께 리다이렉트")
    void loginProcess_Fail() throws Exception {
        given(guestOrderClient.loginGuest(any()))
                .willThrow(new RuntimeException("API Error"));

        mockMvc.perform(post("/orders/guest/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/guest/login?error=true"));
    }

    @Test
    @DisplayName("비회원 주문 준비 페이지 이동")
    void guestOrderPaymentPage_Success() throws Exception {
        OrderPrepareResponseDto responseDto = new OrderPrepareResponseDto(
                List.of(), List.of(), List.of(), List.of(), new CurrentPointResponseDto(0));
        
        given(guestOrderClient.getOrderPrepare(anyString(), any())).willReturn(responseDto);

        mockMvc.perform(post("/guest/orders/prepare")
                        .cookie(new Cookie("GUEST_ID", "guest-uuid"))
                        .flashAttr("orderPrepareRequestDto", new OrderPrepareRequestDto(List.of())))
                .andExpect(status().isOk())
                .andExpect(view().name("orderpayment/OrderPayment"))
                .andExpect(model().attribute("isGuest", true));
    }

    @Test
    @DisplayName("로그인하지 않은 상태에서 주문 이력 접근 시 로그인 리다이렉트")
    void orderHistoryPage_NoToken_Redirect() throws Exception {
        mockMvc.perform(get("/orders/history"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("로그인 상태에서 주문 이력 접근 시 내 주문 목록으로 리다이렉트")
    void orderHistoryPage_WithToken_Redirect() throws Exception {
        mockMvc.perform(get("/orders/history")
                        .cookie(new Cookie("accessToken", "valid-token")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/my-order"));
    }

    @Test
    @DisplayName("비회원 사전 주문 생성 요청")
    void createPreOrder_Guest_Success() throws Exception {
        GuestOrderCreateRequestDto request = mock(GuestOrderCreateRequestDto.class);
        OrderCreateResponseDto response = mock(OrderCreateResponseDto.class);

        given(guestOrderClient.createGuestOrder(anyString(), any())).willReturn(response);

        mockMvc.perform(post("/guest/orders/")
                        .cookie(new Cookie("GUEST_ID", "guest-uuid"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}