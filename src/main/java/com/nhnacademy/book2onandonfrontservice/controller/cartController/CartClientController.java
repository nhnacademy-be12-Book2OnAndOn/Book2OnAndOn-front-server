package com.nhnacademy.book2onandonfrontservice.controller.cartController;

import com.nhnacademy.book2onandonfrontservice.client.CartGuestClient;
import com.nhnacademy.book2onandonfrontservice.client.CartUserClient;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemCountResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemQuantityUpdateRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemSelectAllRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemSelectRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemsResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartMergeResultResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartClientController {

    // 브라우저에서 보내주는 헤더 이름
    private static final String GUEST_ID_HEADER = "X-Guest-Id";

    private final CartUserClient cartUserClient;
    private final CartGuestClient cartGuestClient;

    // =========================
    // 회원 장바구니 API
    // =========================

    // 1. 회원 장바구니 조회
    @GetMapping("/user")
    public CartItemsResponseDto getUserCart(
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        if (accessToken == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }
        return cartUserClient.getUserCart("Bearer " + accessToken);
    }

    // 2. 회원 장바구니 담기
    @PostMapping("/user/items")
    public void addItemToUserCart(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @Valid @RequestBody CartItemRequestDto requestDto
    ) {
        cartUserClient.addItemToUserCart("Bearer " + accessToken, requestDto);
    }

    // 3. 회원 장바구니 수량 변경
    @PatchMapping("/user/items/quantity")
    public void updateUserItemQuantity(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @Valid @RequestBody CartItemQuantityUpdateRequestDto requestDto
    ) {
        cartUserClient.updateUserItemQuantity("Bearer " + accessToken, requestDto);
    }

    // 4. 회원 장바구니 단일 아이템 삭제
    @DeleteMapping("/user/items/{bookId}")
    public void removeItemFromUserCart(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @PathVariable Long bookId
    ) {
        cartUserClient.removeItemFromUserCart("Bearer " + accessToken, bookId);
    }

    // 5. 회원 장바구니 전체 항목 삭제
    @DeleteMapping("/user/items")
    public void clearUserCart(
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        cartUserClient.clearUserCart("Bearer " + accessToken);
    }

    // 6. 회원 장바구니 "선택된" 항목 삭제
    @DeleteMapping("/user/items/selected")
    public void deleteSelectedUserCartItems(
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        cartUserClient.deleteSelectedUserCartItems("Bearer " + accessToken);
    }

    // 7. 회원 장바구니 단건 선택/해제
    @PatchMapping("/user/items/select")
    public void selectUserCartItem(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @Valid @RequestBody CartItemSelectRequestDto requestDto
    ) {
        cartUserClient.selectUserCartItem("Bearer " + accessToken, requestDto);
    }

    // 8. 회원 장바구니 전체 선택/해제
    @PatchMapping("/user/items/select-all")
    public void selectAllUserCartItems(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @Valid @RequestBody CartItemSelectAllRequestDto requestDto
    ) {
        cartUserClient.selectAllUserCartItems("Bearer " + accessToken, requestDto);
    }

    // 9. 아이콘용 장바구니 개수 조회 (회원)
    @GetMapping("/user/count")
    public CartItemCountResponseDto getUserCartCount(
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        return cartUserClient.getUserCartCount("Bearer " + accessToken);
    }

    // 10. 회원 장바구니 중 "선택된 + 구매 가능한" 항목만 조회 (주문용)
    @GetMapping("/user/selected")
    public CartItemsResponseDto getUserSelectedCart(
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        return cartUserClient.getUserSelectedCart("Bearer " + accessToken);
    }

    // 11. 비회원 → 회원 장바구니 병합
    // Frontend는 X-Guest-Id 헤더로 UUID를 보냄 -> @RequestHeader로 수신
    @PostMapping("/user/merge")
    public CartMergeResultResponseDto mergeGuestCartToUserCart(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestHeader(GUEST_ID_HEADER) String uuid
    ) {
        // CartUserClient.merge... 는 @RequestParam으로 uuid를 받도록 정의되어 있음 (이전 코드 기준)
        return cartUserClient.mergeGuestCartToUserCart("Bearer " + accessToken, uuid);
    }

    // =========================
    // 비회원 장바구니 API
    // Frontend는 X-Guest-Id 헤더로 UUID를 보냄 -> @RequestHeader로 수신
    // Gateway는 Cookie를 확인 -> "GUEST_ID=uuid" 쿠키 문자열로 변환하여 Client 호출
    // =========================

    // 1. 비회원 장바구니 조회
    @GetMapping("/guest")
    public CartItemsResponseDto getGuestCart(
            @RequestHeader(GUEST_ID_HEADER) String uuid
    ) {
        return cartGuestClient.getGuestCart("GUEST_ID=" + uuid);
    }

    // 2. 비회원 장바구니 담기
    @PostMapping("/guest/items")
    public void addItemToGuestCart(
            @RequestHeader(GUEST_ID_HEADER) String uuid,
            @Valid @RequestBody CartItemRequestDto requestDto
    ) {
        cartGuestClient.addItemToGuestCart("GUEST_ID=" + uuid, requestDto);
    }

    // 3. 비회원 장바구니 수량 변경
    @PatchMapping("/guest/items/quantity")
    public void updateGuestItemQuantity(
            @RequestHeader(GUEST_ID_HEADER) String uuid,
            @Valid @RequestBody CartItemQuantityUpdateRequestDto requestDto
    ) {
        cartGuestClient.updateGuestItemQuantity("GUEST_ID=" + uuid, requestDto);
    }

    // 4. 비회원 장바구니 단일 아이템 삭제
    @DeleteMapping("/guest/items/{bookId}")
    public void removeItemFromGuestCart(
            @RequestHeader(GUEST_ID_HEADER) String uuid,
            @PathVariable Long bookId
    ) {
        cartGuestClient.removeItemFromGuestCart("GUEST_ID=" + uuid, bookId);
    }

    // 5. 비회원 장바구니 전체 항목 삭제
    @DeleteMapping("/guest/items")
    public void clearGuestCart(
            @RequestHeader(GUEST_ID_HEADER) String uuid
    ) {
        cartGuestClient.clearGuestCart("GUEST_ID=" + uuid);
    }

    // 6. 비회원 장바구니 "선택된" 항목 삭제
    @DeleteMapping("/guest/items/selected")
    public void deleteSelectedGuestCartItems(
            @RequestHeader(GUEST_ID_HEADER) String uuid
    ) {
        cartGuestClient.deleteSelectedGuestCartItems("GUEST_ID=" + uuid);
    }

    // 7. 비회원 장바구니 단건 선택/해제
    @PatchMapping("/guest/items/select")
    public void selectGuestCartItem(
            @RequestHeader(GUEST_ID_HEADER) String uuid,
            @Valid @RequestBody CartItemSelectRequestDto requestDto
    ) {
        cartGuestClient.selectGuestCartItem("GUEST_ID=" + uuid, requestDto);
    }

    // 8. 비회원 장바구니 전체 선택/해제
    @PatchMapping("/guest/items/select-all")
    public void selectAllGuestCartItems(
            @RequestHeader(GUEST_ID_HEADER) String uuid,
            @Valid @RequestBody CartItemSelectAllRequestDto requestDto
    ) {
        cartGuestClient.selectAllGuestCartItems("GUEST_ID=" + uuid, requestDto);
    }

    // 9. 아이콘용 장바구니 개수 조회 (비회원)
    @GetMapping("/guest/count")
    public CartItemCountResponseDto getGuestCartCount(
            @RequestHeader(GUEST_ID_HEADER) String uuid
    ) {
        return cartGuestClient.getGuestCartCount("GUEST_ID=" + uuid);
    }

    // 10. 비회원 장바구니 중 "선택된 + 구매 가능한" 항목만 조회 (주문용)
    @GetMapping("/guest/selected")
    public CartItemsResponseDto getGuestSelectedCart(
            @RequestHeader(GUEST_ID_HEADER) String uuid
    ) {
        return cartGuestClient.getGuestSelectedCart("GUEST_ID=" + uuid);
    }
}