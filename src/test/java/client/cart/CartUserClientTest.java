package client.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.nhnacademy.book2onandonfrontservice.client.CartUserClient;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.*;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartUserClientTest {

    @Mock
    private CartUserClient cartUserClient;

    private final String TOKEN = "Bearer test-access-token";
    private final String GUEST_UUID = "guest-uuid-1234";

    @Test
    @DisplayName("회원 장바구니 조회 ")
    void getUserCart_HappyPath() {
        CartItemsResponseDto responseDto = mock(CartItemsResponseDto.class);
        given(cartUserClient.getUserCart(TOKEN)).willReturn(responseDto);

        CartItemsResponseDto result = cartUserClient.getUserCart(TOKEN);

        assertThat(result).isEqualTo(responseDto);
        verify(cartUserClient).getUserCart(TOKEN);
    }

    @Test
    @DisplayName("회원 장바구니 담기 ")
    void addItemToUserCart_HappyPath() {
        CartItemRequestDto requestDto = new CartItemRequestDto();
        cartUserClient.addItemToUserCart(TOKEN, requestDto);
        verify(cartUserClient).addItemToUserCart(eq(TOKEN), any(CartItemRequestDto.class));
    }

    @Test
    @DisplayName("수량 변경 ")
    void updateQuantity_HappyPath() {
        CartItemQuantityUpdateRequestDto requestDto = new CartItemQuantityUpdateRequestDto();
        cartUserClient.updateQuantityUserCartItem(TOKEN, requestDto);
        verify(cartUserClient).updateQuantityUserCartItem(eq(TOKEN), any(CartItemQuantityUpdateRequestDto.class));
    }

    @Test
    @DisplayName("단일 아이템 삭제 ")
    void deleteItem_HappyPath() {
        Long bookId = 1L;
        cartUserClient.deleteUserCartItem(TOKEN, bookId);
        verify(cartUserClient).deleteUserCartItem(TOKEN, bookId);
    }

    @Test
    @DisplayName("장바구니 전체 삭제 ")
    void clearUserCart_HappyPath() {
        cartUserClient.clearUserCart(TOKEN);
        verify(cartUserClient).clearUserCart(TOKEN);
    }

    @Test
    @DisplayName("선택 항목 삭제 ")
    void deleteSelectedItems_HappyPath() {
        cartUserClient.deleteSelectedUserCartItems(TOKEN);
        verify(cartUserClient).deleteSelectedUserCartItems(TOKEN);
    }

    @Test
    @DisplayName("단건 선택/해제 ")
    void selectItem_HappyPath() {
        CartItemSelectRequestDto requestDto = new CartItemSelectRequestDto();
        cartUserClient.selectUserCartItem(TOKEN, requestDto);
        verify(cartUserClient).selectUserCartItem(eq(TOKEN), any(CartItemSelectRequestDto.class));
    }

    @Test
    @DisplayName("전체 선택/해제 ")
    void selectAllItems_HappyPath() {
        CartItemSelectAllRequestDto requestDto = new CartItemSelectAllRequestDto();
        cartUserClient.selectAllUserCartItems(TOKEN, requestDto);
        verify(cartUserClient).selectAllUserCartItems(eq(TOKEN), any(CartItemSelectAllRequestDto.class));
    }

    @Test
    @DisplayName("장바구니 개수 조회 ")
    void getUserCartCount_HappyPath() {
        CartItemCountResponseDto responseDto = mock(CartItemCountResponseDto.class);
        given(cartUserClient.getUserCartCount(TOKEN)).willReturn(responseDto);

        CartItemCountResponseDto result = cartUserClient.getUserCartCount(TOKEN);

        assertThat(result).isEqualTo(responseDto);
    }

    @Test
    @DisplayName("구매 가능 항목 조회 ")
    void getUserSelectedCart_HappyPath() {
        CartItemsResponseDto responseDto = mock(CartItemsResponseDto.class);
        given(cartUserClient.getUserSelectedCart(TOKEN)).willReturn(responseDto);

        CartItemsResponseDto result = cartUserClient.getUserSelectedCart(TOKEN);

        assertThat(result).isEqualTo(responseDto);
    }

    @Test
    @DisplayName("비회원 장바구니 병합 ")
    void mergeGuestCart_HappyPath() {
        CartMergeResultResponseDto responseDto = mock(CartMergeResultResponseDto.class);
        given(cartUserClient.mergeGuestCartToUserCart(TOKEN, GUEST_UUID)).willReturn(responseDto);

        CartMergeResultResponseDto result = cartUserClient.mergeGuestCartToUserCart(TOKEN, GUEST_UUID);

        assertThat(result).isEqualTo(responseDto);
        verify(cartUserClient).mergeGuestCartToUserCart(TOKEN, GUEST_UUID);
    }

    @Test
    @DisplayName("머지 상태 체크 ")
    void getMergeStatus_HappyPath() {
        CartMergeStatusResponseDto responseDto = mock(CartMergeStatusResponseDto.class);
        given(cartUserClient.getMergeStatus(TOKEN, GUEST_UUID)).willReturn(responseDto);

        CartMergeStatusResponseDto result = cartUserClient.getMergeStatus(TOKEN, GUEST_UUID);

        assertThat(result).isEqualTo(responseDto);
    }

    @Test
    @DisplayName("권한 오류 발생 시나리오 ")
    void unauthorized_FailPath() {
        given(cartUserClient.getUserCart(any())).willThrow(FeignException.Unauthorized.class);

        assertThatThrownBy(() -> cartUserClient.getUserCart("invalid-token"))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("서버 에러 발생 시나리오 ")
    void serverError_FailPath() {
        doThrow(FeignException.InternalServerError.class)
                .when(cartUserClient).clearUserCart(any());

        assertThatThrownBy(() -> cartUserClient.clearUserCart(TOKEN))
                .isInstanceOf(FeignException.class);
    }
}