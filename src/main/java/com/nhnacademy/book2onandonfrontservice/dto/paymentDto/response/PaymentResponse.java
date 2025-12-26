package com.nhnacademy.book2onandonfrontservice.dto.paymentDto.response;

import java.time.LocalDateTime;

public record PaymentResponse(String paymentKey,
                              String orderNumber,
                              Integer totalAmount,
                              String paymentMethod,
                              String paymentProvider,
                              String paymentStatus,
                              LocalDateTime paymentCreatedAt,
                              String paymentReceiptUrl,
                              Integer refundAmount,
                              List<PaymentCancelResponse> paymentCancelResponseList) {
}