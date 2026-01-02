package com.nhnacademy.book2onandonfrontservice.dto.refundDto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원 반품 신청 시 사용
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequestDto {

    private Long orderId;

    private List<RefundItemRequestDto> refundItems; // orderItemId, refundQuantity

    @NotBlank
    private String refundReason;

    private String refundReasonDetail;

}