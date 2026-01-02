package com.nhnacademy.book2onandonfrontservice.controller.cartController;

import com.nhnacademy.book2onandonfrontservice.client.CartGuestClient;
import com.nhnacademy.book2onandonfrontservice.client.CartUserClient;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

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
        String token = toBearer(accessToken);
        if (token == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(cartUserClient.getUserCart(token));
        } catch (feign.FeignException.NotFound e) {
            // 장바구니가 비어있으면 404가 내려올 수 있으므로 빈 객체로 응답
            return ResponseEntity.ok(emptyCart());
        } catch (Exception e) {
            // 병합 체크 호출 시 404 등 예외로 전체 실패하지 않게 예방
            return ResponseEntity.ok(emptyCart());
        }
    }

    @PostMapping("/user/items")
    public ResponseEntity<Void> addItemToUserCart(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @Valid @RequestBody CartItemRequestDto requestDto
    ) {
        String token = toBearer(accessToken);
        if (token == null) {
            return ResponseEntity.status(401).build();
        }
        // 기본값: 담을 때는 선택 상태로
        requestDto.setSelected(true);
        try {
            cartUserClient.addItemToUserCart(token, requestDto);
        } catch (feign.FeignException.NotFound e) {
            // 백엔드가 빈 카트에 대해 404를 주더라도 프론트는 성공으로 간주
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.noContent().build(); // 204
    }

    @PatchMapping("/user/items/quantity")
    public ResponseEntity<Void> updateUserItemQuantity(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @Valid @RequestBody CartItemQuantityUpdateRequestDto requestDto
    ) {
        String token = toBearer(accessToken);
        if (token == null) {
            return ResponseEntity.status(401).build();
        }
        cartUserClient.updateQuantityUserCartItem(token, requestDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/items/{bookId}")
    public ResponseEntity<Void> removeItemFromUserCart(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @PathVariable Long bookId
    ) {
        String token = toBearer(accessToken);
        if (token == null) {
            return ResponseEntity.status(401).build();
        }
        cartUserClient.deleteUserCartItem(token, bookId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/items")
    public ResponseEntity<Void> clearUserCart(
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        String token = toBearer(accessToken);
        if (token == null) {
            return ResponseEntity.status(401).build();
        }
        cartUserClient.clearUserCart(token);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/items/selected")
    public ResponseEntity<Void> deleteSelectedUserCartItems(
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        String token = toBearer(accessToken);
        if (token == null) {
            return ResponseEntity.status(401).build();
        }
        cartUserClient.deleteSelectedUserCartItems(token);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/user/items/select")
    public ResponseEntity<Void> selectUserCartItem(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @Valid @RequestBody CartItemSelectRequestDto requestDto
    ) {
        String token = toBearer(accessToken);
        if (token == null) {
            return ResponseEntity.status(401).build();
        }
        cartUserClient.selectUserCartItem(token, requestDto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/user/items/select-all")
    public ResponseEntity<Void> selectAllUserCartItems(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @Valid @RequestBody CartItemSelectAllRequestDto requestDto
    ) {
        String token = toBearer(accessToken);
        if (token == null) {
            return ResponseEntity.status(401).build();
        }
        cartUserClient.selectAllUserCartItems(token, requestDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/items/count")
    public ResponseEntity<CartItemCountResponseDto> getUserCartCount(
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        String token = toBearer(accessToken);
        if (token == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(cartUserClient.getUserCartCount(token));
        } catch (feign.FeignException.NotFound e) {
            return ResponseEntity.ok(new CartItemCountResponseDto(0, 0));
        } catch (Exception e) {
            return ResponseEntity.ok(new CartItemCountResponseDto(0, 0));
        }
    }

    @GetMapping("/user/selected")
    public ResponseEntity<CartItemsResponseDto> getUserSelectedCart(
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        String token = toBearer(accessToken);
        if (token == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(cartUserClient.getUserSelectedCart(token));
        } catch (feign.FeignException.NotFound e) {
            return ResponseEntity.ok(emptyCart());
        } catch (Exception e) {
            return ResponseEntity.ok(emptyCart());
        }
    }

    @PostMapping("/user/merge")
    public ResponseEntity<CartMergeResultResponseDto> mergeGuestCartToUserCart(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestHeader(GUEST_ID_HEADER) String uuid
    ) {
        String token = toBearer(accessToken);
        if (token == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(cartUserClient.mergeGuestCartToUserCart(token, uuid));
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    @GetMapping("/user/merge-status")
    public ResponseEntity<CartMergeStatusResponseDto> getMergeStatus(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestHeader(GUEST_ID_HEADER) String uuid
    ) {
        String token = toBearer(accessToken);
        if (token == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(cartUserClient.getMergeStatus(token, uuid));
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
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
        // 기본값: 담을 때는 선택 상태로
        requestDto.setSelected(true);
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

    private String toBearer(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return null;
        }
        String decoded = accessToken;
        try {
            decoded = URLDecoder.decode(accessToken, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }
        return decoded.startsWith("Bearer ") ? decoded : "Bearer " + decoded;
    }

    private CartItemsResponseDto emptyCart() {
        return new CartItemsResponseDto(Collections.emptyList(), 0, 0, 0, 0, 0);
    }
}
