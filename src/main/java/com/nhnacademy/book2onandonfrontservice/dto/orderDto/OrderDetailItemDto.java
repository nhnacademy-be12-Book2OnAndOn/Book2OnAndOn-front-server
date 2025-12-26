package com.nhnacademy.book2onandonfrontservice.dto.orderDto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderDetailItemDto {
    private Long orderItemId;  // 주문 상품 고유 ID (취소/반품 시 식별자로 사용)
    private Long bookId;       // 도서 식별자
    private String name;       // 도서 제목
    private Long price;        // 도서 단가
    private Integer quantity;  // 주문 수량
    private Boolean isWrapped; // 포장 여부
    private String wrappingPaperName;   // 선택한 포장지 이름 (포장했을 경우)
    private Long wrappingPaperPrice;    // 포장 비용
}