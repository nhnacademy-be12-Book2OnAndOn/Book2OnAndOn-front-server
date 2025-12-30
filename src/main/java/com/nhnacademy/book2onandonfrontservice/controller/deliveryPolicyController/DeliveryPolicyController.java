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
        Page<DeliveryPolicyDto> page = deliveryPolicyClient.getDeliveryPolicy();
        List<DeliveryPolicyDto> content = page.getContent(); // Page -> List 변환

        log.info("{}", content.size());
        return ResponseEntity.ok(content);
    }

}
