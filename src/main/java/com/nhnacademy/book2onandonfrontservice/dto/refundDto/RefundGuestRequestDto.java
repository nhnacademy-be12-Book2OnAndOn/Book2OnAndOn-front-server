package com.nhnacademy.book2onandonfrontservice.dto.refundDto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 비회원 반품 신청 시 사용
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefundGuestRequestDto {

    private Long orderId;

    private List<RefundItemRequestDto> refundItems; // orderItemId, refundQuantity

    @NotBlank
    private String refundReason;

    private String refundReasonDetail;

    @NotBlank(message = "주문 비밀번호를 입력해주세요.")
    private String guestPassword;

    @NotBlank(message = "주문자 이름을 입력해주세요.")
    private String guestName;

    @NotBlank(message = "주문자 전화번호를 입력해주세요.")
    private String guestPhoneNumber;

}