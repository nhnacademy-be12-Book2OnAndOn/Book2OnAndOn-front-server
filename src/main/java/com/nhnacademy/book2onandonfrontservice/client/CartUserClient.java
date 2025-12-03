package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemCountResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemQuantityUpdateRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemSelectAllRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemSelectRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemsResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartMergeResultResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "gateway-service", contextId = "cartUserClient", url = "${gateway.base-url}")
public interface CartUserClient {

    String USER_ID_HEADER = "X-User-Id";
    String GUEST_ID_HEADER = "X-Guest-Id";

    // 1. 회원 장바구니 조회
    @GetMapping("/api/cart")
    CartItemsResponseDto getUserCart(
            @RequestHeader(USER_ID_HEADER) Long userId
    );

    // 2. 회원 장바구니 담기
    @PostMapping("/api/cart/items")
    void addItemToUserCart(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestBody CartItemRequestDto requestDto
    );

    // 3. 회원 장바구니 수량 변경
    @PatchMapping("/api/cart/items/quantity")
    void updateUserItemQuantity(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestBody CartItemQuantityUpdateRequestDto requestDto
    );

    // 4. 회원 장바구니 단일 아이템 삭제
    @DeleteMapping("/api/cart/items/{bookId}")
    void removeItemFromUserCart(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable("bookId") Long bookId
    );

    // 5. 회원 장바구니 전체 삭제
    @DeleteMapping("/api/cart/items")
    void clearUserCart(
            @RequestHeader(USER_ID_HEADER) Long userId
    );

    // 6. 선택된 항목 삭제
    @DeleteMapping("/api/cart/items/selected")
    void deleteSelectedUserCartItems(
            @RequestHeader(USER_ID_HEADER) Long userId
    );

    // 7. 단건 선택/해제
    @PatchMapping("/api/cart/items/select")
    void selectUserCartItem(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestBody CartItemSelectRequestDto requestDto
    );

    // 8. 전체 선택/해제
    @PatchMapping("/api/cart/items/select-all")
    void selectAllUserCartItems(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestBody CartItemSelectAllRequestDto requestDto
    );

    // 9. 아이콘용 개수 조회
    @GetMapping("/api/cart/count")
    CartItemCountResponseDto getUserCartCount(
            @RequestHeader(USER_ID_HEADER) Long userId
    );

    // 10. 선택 + 구매 가능 항목만 조회 (주문용)
    @GetMapping("/api/cart/selected")
    CartItemsResponseDto getUserSelectedCart(
            @RequestHeader(USER_ID_HEADER) Long userId
    );

    // 11. 비회원 → 회원 병합
    @PostMapping("/api/cart/merge")
    CartMergeResultResponseDto mergeGuestCartToUserCart(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(GUEST_ID_HEADER) String uuid
    );
}
