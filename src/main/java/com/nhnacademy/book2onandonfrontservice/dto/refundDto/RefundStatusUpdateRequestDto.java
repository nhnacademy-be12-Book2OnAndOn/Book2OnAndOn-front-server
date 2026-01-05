package com.nhnacademy.book2onandonfrontservice.dto.refundDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관리자 반품 상태 변경 시 사용
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefundStatusUpdateRequestDto {

    private int statusCode;

}