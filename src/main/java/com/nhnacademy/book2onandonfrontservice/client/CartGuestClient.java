package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemCountResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemQuantityUpdateRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemSelectAllRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemSelectRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemsResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "gateway-service", contextId = "cartGuestClient", url = "${gateway.base-url}")
public interface CartGuestClient {

    String GUEST_ID_HEADER = "X-Guest-Id";

    // 1. 비회원 장바구니 조회
    @GetMapping("/api/cart/guest")
    CartItemsResponseDto getGuestCart(
            @RequestHeader(GUEST_ID_HEADER) String uuid
    );

    // 2. 비회원 장바구니 담기
    @PostMapping("/api/cart/guest/items")
    void addItemToGuestCart(
            @RequestHeader(GUEST_ID_HEADER) String uuid,
            @RequestBody CartItemRequestDto requestDto
    );

    // 3. 비회원 장바구니 수량 변경
    @PatchMapping("/api/cart/guest/items/quantity")
    void updateQuantityGuestCartItem(
            @RequestHeader(GUEST_ID_HEADER) String uuid,
            @RequestBody CartItemQuantityUpdateRequestDto requestDto
    );

    // 4. 비회원 장바구니 단일 아이템 삭제
    @DeleteMapping("/api/cart/guest/items/{bookId}")
    void deleteGuestCartItem(
            @RequestHeader(GUEST_ID_HEADER) String uuid,
            @PathVariable("bookId") Long bookId
    );

    // 5. 비회원 장바구니 전체 삭제
    @DeleteMapping("/api/cart/guest/items")
    void clearGuestCart(
            @RequestHeader(GUEST_ID_HEADER) String uuid
    );

    // 6. 비회원 장바구니 선택 항목 삭제
    @DeleteMapping("/api/cart/guest/items/selected")
    void deleteSelectedGuestCartItems(
            @RequestHeader(GUEST_ID_HEADER) String uuid
    );

    // 7. 비회원 장바구니 단건 선택/해제
    @PatchMapping("/api/cart/guest/items/select")
    void selectGuestCartItem(
            @RequestHeader(GUEST_ID_HEADER) String uuid,
            @RequestBody CartItemSelectRequestDto requestDto
    );

    // 8. 비회원 장바구니 전체 선택/해제
    @PatchMapping("/api/cart/guest/items/select-all")
    void selectAllGuestCartItems(
            @RequestHeader(GUEST_ID_HEADER) String uuid,
            @RequestBody CartItemSelectAllRequestDto requestDto
    );

    // 9. 비회원 장바구니 개수 조회
    @GetMapping("/api/cart/guest/items/count")
    CartItemCountResponseDto getGuestCartCount(
            @RequestHeader(GUEST_ID_HEADER) String uuid
    );

    // 10. 비회원 장바구니 중 선택 + 구매 가능 항목만 조회
    @GetMapping("/api/cart/guest/items/selected")
    CartItemsResponseDto getGuestSelectedCart(
            @RequestHeader(GUEST_ID_HEADER) String uuid
    );
}
