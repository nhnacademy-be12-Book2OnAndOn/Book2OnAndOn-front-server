package com.nhnacademy.book2onandonfrontservice.dto.deliveryDto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDto {

    private Long deliveryId;
    private Long orderId;
    private String orderStatus;
    private String deliveryCompany;
    private String waybill;
    private LocalDateTime deliveryStartedAt;
    private String trackingUrl;
}