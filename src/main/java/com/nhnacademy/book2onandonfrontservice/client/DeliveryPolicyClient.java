package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.deliveryDto.DeliveryPolicyDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "gateway-service", contextId = "deliveryPolicyClient", url = "${gateway.base-url}")
public interface DeliveryPolicyClient {

    @GetMapping("/api/admin/delivery-policies")
    Page<DeliveryPolicyDto> getDeliveryPolicies(@RequestHeader("Authorization") String accessToken,
                                                @RequestParam("page") int page,
                                                @RequestParam("size") int size);

    @GetMapping("/api/admin/delivery-policies/{deliveryPolicyId}")
    DeliveryPolicyDto getDeliveryPolicy(@RequestHeader("Authorization") String accessToken,
                                        @PathVariable("deliveryPolicyId") Long deliveryPolicyId);


    @PostMapping("/api/admin/delivery-policies")
    void createDeliveryPolicy(@RequestHeader("Authorization") String accessToken,
                              @RequestBody DeliveryPolicyDto requestDto);

    @PutMapping("/api/admin/delivery-policies/{deliveryPolicyId}")
    void updateDeliveryPolicy(@RequestHeader("Authorization") String accessToken,
                              @PathVariable("deliveryPolicyId") Long deliveryPolicyId,
                              @RequestBody DeliveryPolicyDto requestDto);

    @GetMapping("/api/delivery-policies")
    Page<DeliveryPolicyDto> getDeliveryPolicy(@RequestParam("page") int page,
                                              @RequestParam("size") int size);
}
