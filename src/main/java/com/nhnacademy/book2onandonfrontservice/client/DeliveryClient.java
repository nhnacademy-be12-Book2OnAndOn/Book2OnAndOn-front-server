package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.deliveryDto.DeliveryDto;
import com.nhnacademy.book2onandonfrontservice.dto.deliveryDto.DeliveryWaybillUpdateDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.status.OrderStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "gateway-service", contextId = "deliveryClient", url = "${gateway.base-url}")
public interface DeliveryClient {

    //주문 번호로 배송정보 조회
    @GetMapping("/api/deliveries")
    DeliveryDto getDeliveryByOrder(@RequestParam("orderId") Long orderId,
                                   @RequestHeader("Authorization") String accessToken);


    // admin 배송 목록 조회 (페이징 + 상태 필터링)
    @GetMapping("/api/admin/deliveries")
    Page<DeliveryDto> getDeliveries(@RequestHeader("Authorization") String accessToken,
                                    @RequestParam("page") int page,
                                    @RequestParam("size") int size,
                                    @RequestParam(value = "status", required = false) OrderStatus status);

    // admin 운송장 등록 (배송 시작)
    @PutMapping("/api/admin/deliveries/{deliveryId}/waybill")
    void registerWaybill(@RequestHeader("Authorization") String accessToken,
                         @PathVariable("deliveryId") Long deliveryId,
                         @RequestBody DeliveryWaybillUpdateDto requestDto);

    // admin 배송 정보 수정
    @PutMapping("/api/admin/deliveries/{deliveryId}/info")
    void updateDeliveryInfo(@RequestHeader("Authorization") String accessToken,
                            @PathVariable("deliveryId") Long deliveryId,
                            @RequestBody DeliveryWaybillUpdateDto requestDto);
}