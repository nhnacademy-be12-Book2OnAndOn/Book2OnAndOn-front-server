package com.nhnacademy.book2onandonfrontservice.controller.deliveryPolicyController;

import com.nhnacademy.book2onandonfrontservice.client.DeliveryPolicyClient;
import com.nhnacademy.book2onandonfrontservice.dto.deliveryDto.DeliveryPolicyDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DeliveryPolicyController {
    private final DeliveryPolicyClient deliveryPolicyClient;

    @GetMapping("/delivery-policies")
    public ResponseEntity<List<DeliveryPolicyDto>> getDeliveryPoliciesSimple(){

        log.info("delivery-policies 들어옴");

        int page = 0;
        int size = 10;

        Page<DeliveryPolicyDto> pages = deliveryPolicyClient.getDeliveryPolicy(page, size);

        log.info("page : {}", page);

        List<DeliveryPolicyDto> content = pages.getContent(); // Page -> List 변환

        return ResponseEntity.ok(content);
    }

}
