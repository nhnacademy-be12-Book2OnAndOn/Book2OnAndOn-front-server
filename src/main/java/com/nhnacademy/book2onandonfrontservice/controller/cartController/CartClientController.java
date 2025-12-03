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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartClientController {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String GUEST_ID_HEADER = "X-Guest-Id";

    private final CartUserClient cartUserClient;
    private final CartGuestClient cartGuestClient;

    // =========================
    // 회원 장바구니 API
    // =========================

    // 1. 회원 장바구니 조회
    // GET /cart/user
    @GetMapping("/user")
    public CartItemsResponseDto getUserCart(
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        return cartUserClient.getUserCart(userId);
    }

    // 2. 회원 장바구니 담기
    // POST /cart/user/items
    @PostMapping("/user/items")
    public void addItemToUserCart(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody CartItemRequestDto requestDto
    ) {
        cartUserClient.addItemToUserCart(userId, requestDto);
    }

    // 3. 회원 장바구니 수량 변경 (절대값 변경)
    // PATCH /cart/user/items/quantity
    @PatchMapping("/user/items/quantity")
    public void updateUserItemQuantity(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody CartItemQuantityUpdateRequestDto requestDto
    ) {
        cartUserClient.updateUserItemQuantity(userId, requestDto);
    }

    // 4. 회원 장바구니 단일 아이템 삭제
    // DELETE /cart/user/items/{bookId}
    @DeleteMapping("/user/items/{bookId}")
    public void removeItemFromUserCart(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long bookId
    ) {
        cartUserClient.removeItemFromUserCart(userId, bookId);
    }

    // 5. 회원 장바구니 전체 항목 삭제
    // DELETE /cart/user/items
    @DeleteMapping("/user/items")
    public void clearUserCart(
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        cartUserClient.clearUserCart(userId);
    }

    // 6. 회원 장바구니 "선택된" 항목 삭제
    // DELETE /cart/user/items/selected
    @DeleteMapping("/user/items/selected")
    public void deleteSelectedUserCartItems(
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        cartUserClient.deleteSelectedUserCartItems(userId);
    }

    // 7. 회원 장바구니 단건 선택/해제
    // PATCH /cart/user/items/select
    @PatchMapping("/user/items/select")
    public void selectUserCartItem(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody CartItemSelectRequestDto requestDto
    ) {
        cartUserClient.selectUserCartItem(userId, requestDto);
    }

    // 8. 회원 장바구니 전체 선택/해제
    // PATCH /cart/user/items/select-all
    @PatchMapping("/user/items/select-all")
    public void selectAllUserCartItems(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody CartItemSelectAllRequestDto requestDto
    ) {
        cartUserClient.selectAllUserCartItems(userId, requestDto);
    }

    // 9. 아이콘용 장바구니 개수 조회 (회원)
    // GET /cart/user/count
    @GetMapping("/user/count")
    public CartItemCountResponseDto getUserCartCount(
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        return cartUserClient.getUserCartCount(userId);
    }

    // 10. 회원 장바구니 중 "선택된 + 구매 가능한" 항목만 조회 (주문용)
    // GET /cart/user/selected
    @GetMapping("/user/selected")
    public CartItemsResponseDto getUserSelectedCart(
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        return cartUserClient.getUserSelectedCart(userId);
    }

    // 11. 비회원 → 회원 장바구니 병합
    // POST /cart/user/merge
    @PostMapping("/user/merge")
    public CartMergeResultResponseDto mergeGuestCartToUserCart(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(GUEST_ID_HEADER) String uuid
    ) {
        return cartUserClient.mergeGuestCartToUserCart(userId, uuid);
    }

    // =========================
    // 비회원 장바구니 API
    // =========================

    // 1. 비회원 장바구니 조회
    // GET /cart/guest?uuid=xxx
    @GetMapping("/guest")
    public CartItemsResponseDto getGuestCart(
            @RequestParam(GUEST_ID_HEADER) String uuid
    ) {
        return cartGuestClient.getGuestCart(uuid);
    }

    // 2. 비회원 장바구니 담기
    // POST /cart/guest/items?uuid=xxx
    @PostMapping("/guest/items")
    public void addItemToGuestCart(
            @RequestParam(GUEST_ID_HEADER) String uuid,
            @Valid @RequestBody CartItemRequestDto requestDto
    ) {
        cartGuestClient.addItemToGuestCart(uuid, requestDto);
    }

    // 3. 비회원 장바구니 수량 변경
    // PATCH /cart/guest/items/quantity?uuid=xxx
    @PatchMapping("/guest/items/quantity")
    public void updateGuestItemQuantity(
            @RequestParam(GUEST_ID_HEADER) String uuid,
            @Valid @RequestBody CartItemQuantityUpdateRequestDto requestDto
    ) {
        cartGuestClient.updateGuestItemQuantity(uuid, requestDto);
    }

    // 4. 비회원 장바구니 단일 아이템 삭제
    // DELETE /cart/guest/items/{bookId}?uuid=xxx
    @DeleteMapping("/guest/items/{bookId}")
    public void removeItemFromGuestCart(
            @RequestParam(GUEST_ID_HEADER) String uuid,
            @PathVariable Long bookId
    ) {
        cartGuestClient.removeItemFromGuestCart(uuid, bookId);
    }

    // 5. 비회원 장바구니 전체 항목 삭제
    // DELETE /cart/guest/items?uuid=xxx
    @DeleteMapping("/guest/items")
    public void clearGuestCart(
            @RequestParam(GUEST_ID_HEADER) String uuid
    ) {
        cartGuestClient.clearGuestCart(uuid);
    }

    // 6. 비회원 장바구니 "선택된" 항목 삭제
    // DELETE /cart/guest/items/selected?uuid=xxx
    @DeleteMapping("/guest/items/selected")
    public void deleteSelectedGuestCartItems(
            @RequestParam(GUEST_ID_HEADER) String uuid
    ) {
        cartGuestClient.deleteSelectedGuestCartItems(uuid);
    }

    // 7. 비회원 장바구니 단건 선택/해제
    // PATCH /cart/guest/items/select?uuid=xxx
    @PatchMapping("/guest/items/select")
    public void selectGuestCartItem(
            @RequestParam(GUEST_ID_HEADER) String uuid,
            @Valid @RequestBody CartItemSelectRequestDto requestDto
    ) {
        cartGuestClient.selectGuestCartItem(uuid, requestDto);
    }

    // 8. 비회원 장바구니 전체 선택/해제
    // PATCH /cart/guest/items/select-all?uuid=xxx
    @PatchMapping("/guest/items/select-all")
    public void selectAllGuestCartItems(
            @RequestParam(GUEST_ID_HEADER) String uuid,
            @Valid @RequestBody CartItemSelectAllRequestDto requestDto
    ) {
        cartGuestClient.selectAllGuestCartItems(uuid, requestDto);
    }

    // 9. 아이콘용 장바구니 개수 조회 (비회원)
    // GET /cart/guest/count?uuid=xxx
    @GetMapping("/guest/count")
    public CartItemCountResponseDto getGuestCartCount(
            @RequestParam(GUEST_ID_HEADER) String uuid
    ) {
        return cartGuestClient.getGuestCartCount(uuid);
    }

    // 10. 비회원 장바구니 중 "선택된 + 구매 가능한" 항목만 조회 (주문용)
    // GET /cart/guest/selected?uuid=xxx
    @GetMapping("/guest/selected")
    public CartItemsResponseDto getGuestSelectedCart(
            @RequestParam(GUEST_ID_HEADER) String uuid
    ) {
        return cartGuestClient.getGuestSelectedCart(uuid);
    }
}
