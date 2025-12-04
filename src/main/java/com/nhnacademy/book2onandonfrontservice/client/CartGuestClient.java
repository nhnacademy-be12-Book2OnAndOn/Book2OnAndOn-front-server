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


    // 1. 비회원 장바구니 조회
    @GetMapping("/api/cart/guest")
    CartItemsResponseDto getGuestCart(
            @RequestHeader("Cookie") String guestCookie
    );

    // 2. 비회원 장바구니 담기
    @PostMapping("/api/cart/guest/items")
    void addItemToGuestCart(
            @RequestHeader("Cookie") String guestCookie,
            @RequestBody CartItemRequestDto requestDto
    );

    // 3. 비회원 장바구니 수량 변경
    @PatchMapping("/api/cart/guest/items/quantity")
    void updateGuestItemQuantity(
            @RequestHeader("Cookie") String guestCookie,
            @RequestBody CartItemQuantityUpdateRequestDto requestDto
    );

    // 4. 비회원 장바구니 단일 아이템 삭제
    @DeleteMapping("/api/cart/guest/items/{bookId}")
    void removeItemFromGuestCart(
            @RequestHeader("Cookie") String guestCookie,
            @PathVariable("bookId") Long bookId
    );

    // 5. 비회원 장바구니 전체 삭제
    @DeleteMapping("/api/cart/guest/items")
    void clearGuestCart(
            @RequestHeader("Cookie") String guestCookie
    );

    // 6. 비회원 장바구니 선택 항목 삭제
    @DeleteMapping("/api/cart/guest/items/selected")
    void deleteSelectedGuestCartItems(
            @RequestHeader("Cookie") String guestCookie
    );

    // 7. 비회원 장바구니 단건 선택/해제
    @PatchMapping("/api/cart/guest/items/select")
    void selectGuestCartItem(
            @RequestHeader("Cookie") String guestCookie,
            @RequestBody CartItemSelectRequestDto requestDto
    );

    // 8. 비회원 장바구니 전체 선택/해제
    @PatchMapping("/api/cart/guest/items/select-all")
    void selectAllGuestCartItems(
            @RequestHeader("Cookie") String guestCookie,
            @RequestBody CartItemSelectAllRequestDto requestDto
    );

    // 9. 비회원 장바구니 개수 조회
    @GetMapping("/api/cart/guest/count")
    CartItemCountResponseDto getGuestCartCount(
            @RequestHeader("Cookie") String guestCookie
    );

    // 10. 비회원 장바구니 중 선택 + 구매 가능 항목만 조회
    @GetMapping("/api/cart/guest/selected")
    CartItemsResponseDto getGuestSelectedCart(
            @RequestHeader("Cookie") String guestCookie
    );
}
