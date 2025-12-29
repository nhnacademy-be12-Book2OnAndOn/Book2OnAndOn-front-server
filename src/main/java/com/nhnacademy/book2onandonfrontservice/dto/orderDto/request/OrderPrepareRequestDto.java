package com.nhnacademy.book2onandonfrontservice.dto.orderDto.request;

import com.nhnacademy.book2onandonfrontservice.dto.orderDto.BookInfoDto;
import java.util.List;

/**
 * 장바구니 혹은 바로 구매시 주문항목 리스트
 * @param bookItems
 */
public record OrderPrepareRequestDto(List<BookInfoDto> bookItems) {
}
