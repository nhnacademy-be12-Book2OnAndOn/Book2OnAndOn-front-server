package com.nhnacademy.book2onandonfrontservice.dto.orderDto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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