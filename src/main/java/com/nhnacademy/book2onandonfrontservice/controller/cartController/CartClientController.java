package com.nhnacademy.book2onandonfrontservice.controller.cartController;

import com.nhnacademy.book2onandonfrontservice.client.CartGuestClient;
import com.nhnacademy.book2onandonfrontservice.client.CartUserClient;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/cart", produces = "application/json")
@RequiredArgsConstructor
public class CartClientController {

    private static final String GUEST_ID_HEADER = "X-Guest-Id";

    private final CartUserClient cartUserClient;
    private final CartGuestClient cartGuestClient;

    // =========================
    // 회원 장바구니 API
    // =========================

    @GetMapping("/user")
    public ResponseEntity<CartItemsResponseDto> getUserCart(
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        if (accessToken == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(cartUserClient.getUserCart("Bearer " + accessToken));
    }

    @PostMapping("/user/items")
    public ResponseEntity<Void> addItemToUserCart(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @Valid @RequestBody CartItemRequestDto requestDto
    ) {
        if (accessToken == null) {
            return ResponseEntity.status(401).build();
        }
        cartUserClient.addItemToUserCart("Bearer " + accessToken, requestDto);
        return ResponseEntity.noContent().build(); // 204
    }

    @PatchMapping("/user/items/quantity")
    public ResponseEntity<Void> updateUserItemQuantity(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @Valid @RequestBody CartItemQuantityUpdateRequestDto requestDto
    ) {
        if (accessToken == null) {
            return ResponseEntity.status(401).build();
        }
        cartUserClient.updateQuantityUserCartItem("Bearer " + accessToken, requestDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/items/{bookId}")
    public ResponseEntity<Void> removeItemFromUserCart(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @PathVariable Long bookId
    ) {
        if (accessToken == null) {
            return ResponseEntity.status(401).build();
        }
        cartUserClient.deleteUserCartItem("Bearer " + accessToken, bookId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/items")
    public ResponseEntity<Void> clearUserCart(
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        if (accessToken == null) {
            return ResponseEntity.status(401).build();
        }
        cartUserClient.clearUserCart("Bearer " + accessToken);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/items/selected")
    public ResponseEntity<Void> deleteSelectedUserCartItems(
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        if (accessToken == null) {
            return ResponseEntity.status(401).build();
        }
        cartUserClient.deleteSelectedUserCartItems("Bearer " + accessToken);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/user/items/select")
    public ResponseEntity<Void> selectUserCartItem(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @Valid @RequestBody CartItemSelectRequestDto requestDto
    ) {
        if (accessToken == null) {
            return ResponseEntity.status(401).build();
        }
        cartUserClient.selectUserCartItem("Bearer " + accessToken, requestDto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/user/items/select-all")
    public ResponseEntity<Void> selectAllUserCartItems(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @Valid @RequestBody CartItemSelectAllRequestDto requestDto
    ) {
        if (accessToken == null) {
            return ResponseEntity.status(401).build();
        }
        cartUserClient.selectAllUserCartItems("Bearer " + accessToken, requestDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/items/count")
    public ResponseEntity<CartItemCountResponseDto> getUserCartCount(
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        if (accessToken == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(cartUserClient.getUserCartCount("Bearer " + accessToken));
    }

    @GetMapping("/user/selected")
    public ResponseEntity<CartItemsResponseDto> getUserSelectedCart(
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        if (accessToken == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(cartUserClient.getUserSelectedCart("Bearer " + accessToken));
    }

    @PostMapping("/user/merge")
    public ResponseEntity<CartMergeResultResponseDto> mergeGuestCartToUserCart(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestHeader(GUEST_ID_HEADER) String uuid
    ) {
        if (accessToken == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(cartUserClient.mergeGuestCartToUserCart("Bearer " + accessToken, uuid));
    }

    @GetMapping("/user/merge-status")
    public ResponseEntity<CartMergeStatusResponseDto> getMergeStatus(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestHeader(GUEST_ID_HEADER) String uuid
    ) {
        if (accessToken == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(cartUserClient.getMergeStatus("Bearer " + accessToken, uuid));
    }


    // =========================
    // 비회원 장바구니 API
    // =========================

    @GetMapping("/guest")
    public ResponseEntity<CartItemsResponseDto> getGuestCart(
            @RequestHeader(GUEST_ID_HEADER) String uuid
    ) {
        return ResponseEntity.ok(cartGuestClient.getGuestCart(uuid));
    }

    @PostMapping("/guest/items")
    public ResponseEntity<Void> addItemToGuestCart(
            @RequestHeader(GUEST_ID_HEADER) String uuid,
            @Valid @RequestBody CartItemRequestDto requestDto
    ) {
        cartGuestClient.addItemToGuestCart(uuid, requestDto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/guest/items/quantity")
    public ResponseEntity<Void> updateQuantityGuestCartItem(
            @RequestHeader(GUEST_ID_HEADER) String uuid,
            @Valid @RequestBody CartItemQuantityUpdateRequestDto requestDto
    ) {
        cartGuestClient.updateQuantityGuestCartItem(uuid, requestDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/guest/items/{bookId}")
    public ResponseEntity<Void> deleteGuestCartItem(
            @RequestHeader(GUEST_ID_HEADER) String uuid,
            @PathVariable Long bookId
    ) {
        cartGuestClient.deleteGuestCartItem(uuid, bookId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/guest/items")
    public ResponseEntity<Void> clearGuestCart(
            @RequestHeader(GUEST_ID_HEADER) String uuid
    ) {
        cartGuestClient.clearGuestCart(uuid);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/guest/items/selected")
    public ResponseEntity<Void> deleteSelectedGuestCartItems(
            @RequestHeader(GUEST_ID_HEADER) String uuid
    ) {
        cartGuestClient.deleteSelectedGuestCartItems(uuid);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/guest/items/select")
    public ResponseEntity<Void> selectGuestCartItem(
            @RequestHeader(GUEST_ID_HEADER) String uuid,
            @Valid @RequestBody CartItemSelectRequestDto requestDto
    ) {
        cartGuestClient.selectGuestCartItem(uuid, requestDto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/guest/items/select-all")
    public ResponseEntity<Void> selectAllGuestCartItems(
            @RequestHeader(GUEST_ID_HEADER) String uuid,
            @Valid @RequestBody CartItemSelectAllRequestDto requestDto
    ) {
        cartGuestClient.selectAllGuestCartItems(uuid, requestDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/guest/items/count")
    public ResponseEntity<CartItemCountResponseDto> getGuestCartCount(
            @RequestHeader(GUEST_ID_HEADER) String uuid
    ) {
        return ResponseEntity.ok(cartGuestClient.getGuestCartCount(uuid));
    }

    @GetMapping("/guest/selected")
    public ResponseEntity<CartItemsResponseDto> getGuestSelectedCart(
            @RequestHeader(GUEST_ID_HEADER) String uuid
    ) {
        return ResponseEntity.ok(cartGuestClient.getGuestSelectedCart(uuid));
    }
}
