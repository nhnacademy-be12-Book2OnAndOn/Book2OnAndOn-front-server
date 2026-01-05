package com.nhnacademy.book2onandonfrontservice.dto.refundDto;

import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.RefundItemResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 반품 신청, 조회, 상태 변경 시 사용
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponseDto {
    private Long refundId;
    private Long orderId;
    private String refundReason;
    private String refundReasonDetail; // 반품 사유 상세 내용
    private String refundStatus; // RefundStatus Enum의 설명 필드
    private LocalDateTime refundCreatedAt;

    // 반품 항목 리스트
    private List<RefundItemResponseDto> refundItems;
}