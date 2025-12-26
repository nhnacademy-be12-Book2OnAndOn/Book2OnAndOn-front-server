package com.nhnacademy.book2onandonfrontservice.dto.paymentDto.response;

import java.time.LocalDateTime;

public record PaymentCancelResponse(String paymentKey,
                                    Integer cancelAmount,
                                    String cancelReason,
                                    LocalDateTime canceledAt) {
}
