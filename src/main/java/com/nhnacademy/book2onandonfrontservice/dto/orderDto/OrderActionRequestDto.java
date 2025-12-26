package com.nhnacademy.book2onandonfrontservice.dto.orderDto;

import java.util.List;

public class OrderActionRequestDto {
    private List<Long> orderItemIdList;     // 취소,반품할 항목 ID 리스트
    private String reason;                  // 사용자 입력 사유
    private String returnReason;            // 반품 사유
    private Long cancelAmount;              // 취소 예정 금액
}