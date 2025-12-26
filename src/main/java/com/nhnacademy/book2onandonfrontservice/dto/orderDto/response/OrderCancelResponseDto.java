package com.nhnacademy.book2onandonfrontservice.dto.orderDto.response;

import com.nhnacademy.book2onandonfrontservice.dto.paymentDto.response.PaymentCancelResponse;
import java.util.List;

public record OrderCancelResponseDto(String orderNumber,
                                     String orderStatus,
                                     List<PaymentCancelResponse> paymentCancelResponseList) {
}