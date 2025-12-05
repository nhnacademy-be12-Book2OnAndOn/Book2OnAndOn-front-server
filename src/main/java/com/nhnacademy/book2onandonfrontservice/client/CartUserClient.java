package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemCountResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemQuantityUpdateRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemSelectAllRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemSelectRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartItemsResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartMergeResultResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.cartDto.CartMergeStatusResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "gateway-service", contextId = "cartUserClient", url = "${gateway.base-url}")
public interface CartUserClient {

    String GUEST_ID_HEADER = "X-Guest-Id";

    // 1. 회원 장바구니 조회
    @GetMapping("/api/cart/user")
    CartItemsResponseDto getUserCart(
            @RequestHeader("Authorization") String accessToken
    );

    // 2. 회원 장바구니 담기
    @PostMapping("/api/cart/user/items")
    void addItemToUserCart(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody CartItemRequestDto requestDto
    );

    // 3. 회원 장바구니 수량 변경
    @PatchMapping("/api/cart/user/items/quantity")
    void updateQuantityUserCartItem(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody CartItemQuantityUpdateRequestDto requestDto
    );

    // 4. 회원 장바구니 단일 아이템 삭제
    @DeleteMapping("/api/cart/user/items/{bookId}")
    void deleteUserCartItem(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("bookId") Long bookId
    );

    // 5. 회원 장바구니 전체 삭제
    @DeleteMapping("/api/cart/user/items")
    void clearUserCart(
            @RequestHeader("Authorization") String accessToken
    );

    // 6. 선택된 항목 삭제
    @DeleteMapping("/api/cart/user/items/selected")
    void deleteSelectedUserCartItems(
            @RequestHeader("Authorization") String accessToken
    );

    // 7. 단건 선택/해제
    @PatchMapping("/api/cart/user/items/select")
    void selectUserCartItem(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody CartItemSelectRequestDto requestDto
    );

    // 8. 전체 선택/해제
    @PatchMapping("/api/cart/user/items/select-all")
    void selectAllUserCartItems(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody CartItemSelectAllRequestDto requestDto
    );

    // 9. 아이콘용 개수 조회
    @GetMapping("/api/cart/user/items/count")
    CartItemCountResponseDto getUserCartCount(
            @RequestHeader("Authorization") String accessToken
    );

    // 10. 선택 + 구매 가능 항목만 조회 (주문용)
    @GetMapping("/api/cart/user/items/selected")
    CartItemsResponseDto getUserSelectedCart(
            @RequestHeader("Authorization") String accessToken
    );

    // 11. 비회원 → 회원 병합
    @PostMapping("/api/cart/user/merge")
    CartMergeResultResponseDto mergeGuestCartToUserCart(
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader(GUEST_ID_HEADER) String uuid
    );

    // 12. 머지 체크용
    // GET /api/cart/user/merge-status
    @GetMapping("/api/cart/user/merge-status")
    CartMergeStatusResponseDto getMergeStatus(
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader(GUEST_ID_HEADER) String uuid
    );
}
