package client.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.nhnacademy.book2onandonfrontservice.client.CartGuestClient;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemCountResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemQuantityUpdateRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemSelectAllRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemSelectRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemsResponseDto;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartGuestClientTest {

    @Mock
    private CartGuestClient cartGuestClient;

    private final String UUID = "test-uuid-1234";

    @Test
    @DisplayName("비회원 장바구니 조회 ")
    void getGuestCart_HappyPath() {
        CartItemsResponseDto expectedResponse = mock(CartItemsResponseDto.class);
        given(cartGuestClient.getGuestCart(UUID)).willReturn(expectedResponse);

        CartItemsResponseDto result = cartGuestClient.getGuestCart(UUID);

        assertThat(result).isEqualTo(expectedResponse);
        verify(cartGuestClient).getGuestCart(UUID);
    }

    @Test
    @DisplayName("비회원 장바구니 아이템 추가 ")
    void addItemToGuestCart_HappyPath() {
        CartItemRequestDto requestDto = new CartItemRequestDto();

        cartGuestClient.addItemToGuestCart(UUID, requestDto);

        verify(cartGuestClient).addItemToGuestCart(eq(UUID), any(CartItemRequestDto.class));
    }

    @Test
    @DisplayName("비회원 장바구니 아이템 추가  (인증 오류 등)")
    void addItemToGuestCart_FailPath() {
        CartItemRequestDto requestDto = new CartItemRequestDto();
        doThrow(FeignException.Unauthorized.class)
            .when(cartGuestClient).addItemToGuestCart(eq(UUID), any(CartItemRequestDto.class));

        assertThatThrownBy(() -> cartGuestClient.addItemToGuestCart(UUID, requestDto))
            .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("수량 변경 ")
    void updateQuantity_HappyPath() {
        CartItemQuantityUpdateRequestDto requestDto = new CartItemQuantityUpdateRequestDto();

        cartGuestClient.updateQuantityGuestCartItem(UUID, requestDto);

        verify(cartGuestClient).updateQuantityGuestCartItem(eq(UUID), any(CartItemQuantityUpdateRequestDto.class));
    }

    @Test
    @DisplayName("단일 아이템 삭제 ")
    void deleteItem_HappyPath() {
        Long bookId = 1L;

        cartGuestClient.deleteGuestCartItem(UUID, bookId);

        verify(cartGuestClient).deleteGuestCartItem(UUID, bookId);
    }

    @Test
    @DisplayName("장바구니 전체 비우기 ")
    void clearGuestCart_HappyPath() {
        cartGuestClient.clearGuestCart(UUID);

        verify(cartGuestClient).clearGuestCart(UUID);
    }

    @Test
    @DisplayName("선택 항목 삭제 ")
    void deleteSelectedItems_HappyPath() {
        cartGuestClient.deleteSelectedGuestCartItems(UUID);

        verify(cartGuestClient).deleteSelectedGuestCartItems(UUID);
    }

    @Test
    @DisplayName("아이템 선택/해제 ")
    void selectItem_HappyPath() {
        CartItemSelectRequestDto requestDto = new CartItemSelectRequestDto();

        cartGuestClient.selectGuestCartItem(UUID, requestDto);

        verify(cartGuestClient).selectGuestCartItem(eq(UUID), any(CartItemSelectRequestDto.class));
    }

    @Test
    @DisplayName("전체 아이템 선택/해제 ")
    void selectAllItems_HappyPath() {
        CartItemSelectAllRequestDto requestDto = new CartItemSelectAllRequestDto();

        cartGuestClient.selectAllGuestCartItems(UUID, requestDto);

        verify(cartGuestClient).selectAllGuestCartItems(eq(UUID), any(CartItemSelectAllRequestDto.class));
    }

    @Test
    @DisplayName("장바구니 아이템 개수 조회 ")
    void getCartCount_HappyPath() {
        CartItemCountResponseDto expectedResponse = mock(CartItemCountResponseDto.class);
        given(cartGuestClient.getGuestCartCount(UUID)).willReturn(expectedResponse);

        CartItemCountResponseDto result = cartGuestClient.getGuestCartCount(UUID);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("선택된 아이템 목록 조회 ")
    void getSelectedCart_HappyPath() {
        CartItemsResponseDto expectedResponse = mock(CartItemsResponseDto.class);
        given(cartGuestClient.getGuestSelectedCart(UUID)).willReturn(expectedResponse);

        CartItemsResponseDto result = cartGuestClient.getGuestSelectedCart(UUID);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("조회 실패 (404 Not Found)")
    void getGuestCart_FailPath() {
        given(cartGuestClient.getGuestCart(UUID)).willThrow(FeignException.NotFound.class);

        assertThatThrownBy(() -> cartGuestClient.getGuestCart(UUID))
            .isInstanceOf(FeignException.class);
    }
}