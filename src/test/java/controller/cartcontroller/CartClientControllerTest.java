package com.nhnacademy.book2onandonfrontservice.controller.cartController;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.book2onandonfrontservice.client.BookClient; // 추가
import com.nhnacademy.book2onandonfrontservice.client.CartGuestClient;
import com.nhnacademy.book2onandonfrontservice.client.CartUserClient;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.*;
import jakarta.servlet.http.Cookie;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser
class CartClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartUserClient cartUserClient;

    @MockBean
    private CartGuestClient cartGuestClient;

    @MockBean
    private BookClient bookClient;

    @Autowired
    private ObjectMapper objectMapper;

    private final String ACCESS_TOKEN = "test-token";
    private final String GUEST_ID = "guest-uuid";

    @Test
    @DisplayName("회원 장바구니 조회")
    void getUserCart_Success() throws Exception {
        CartItemsResponseDto response = new CartItemsResponseDto(Collections.emptyList(), 0, 0, 0, 0, 0);
        given(cartUserClient.getUserCart(anyString())).willReturn(response);

        mockMvc.perform(get("/cart/user")
                        .cookie(new Cookie("accessToken", ACCESS_TOKEN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(0));
    }

    @Test
    @DisplayName("회원 장바구니 아이템 개수 조회")
    void getUserCartCount_Success() throws Exception {
        given(cartUserClient.getUserCartCount(anyString())).willReturn(new CartItemCountResponseDto(3, 5));

        mockMvc.perform(get("/cart/user/items/count")
                        .cookie(new Cookie("accessToken", ACCESS_TOKEN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(3));
    }

    @Test
    @DisplayName("회원 장바구니 조회 실패 시 빈 장바구니 반환")
    void getUserCart_Fail_ReturnsEmpty() throws Exception {
        given(cartUserClient.getUserCart(anyString())).willThrow(feign.FeignException.NotFound.class);

        mockMvc.perform(get("/cart/user")
                        .cookie(new Cookie("accessToken", ACCESS_TOKEN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    @DisplayName("회원 장바구니 병합 요청")
    void mergeGuestCartToUserCart_Success() throws Exception {
        CartMergeResultResponseDto response = new CartMergeResultResponseDto(
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), true);

        given(cartUserClient.mergeGuestCartToUserCart(anyString(), anyString()))
                .willReturn(response);

        mockMvc.perform(post("/cart/user/merge")
                        .cookie(new Cookie("accessToken", ACCESS_TOKEN))
                        .header("X-Guest-Id", GUEST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mergeSucceeded").value(true));
    }

    @Test
    @DisplayName("토큰 없을 때 회원 장바구니 조회 실패")
    void getUserCart_NoToken() throws Exception {
        mockMvc.perform(get("/cart/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("비회원 장바구니 조회")
    void getGuestCart_Success() throws Exception {
        CartItemsResponseDto response = new CartItemsResponseDto(Collections.emptyList(), 0, 0, 0, 0, 0);
        given(cartGuestClient.getGuestCart(eq(GUEST_ID))).willReturn(response);

        mockMvc.perform(get("/cart/guest")
                        .header("X-Guest-Id", GUEST_ID))
                .andExpect(status().isOk());
    }
}
