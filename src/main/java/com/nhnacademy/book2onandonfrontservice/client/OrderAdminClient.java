package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderItemStatusUpdateDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderStatusUpdateDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderDetailResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderSimpleDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "gateway-service", contextId = "OrderAdminClient", url = "${gateway.base-url}")
public interface OrderAdminClient {
    @GetMapping("/api/admin/orders")
    PageResponse<OrderSimpleDto> getOrderList(Pageable pageable);

    @GetMapping("/api/admin/orders/{orderNumber}")
    OrderDetailResponseDto getOrderDetail(@PathVariable("orderNumber") String orderNumber);

    @PatchMapping("/api/admin/orders/{orderNumber}")
    void updateOrderStatus(@PathVariable("orderNumber") String orderNumber,
                           @RequestBody OrderStatusUpdateDto req);

    @PatchMapping("/api/admin/orders/{orderNumber}/order-items")
    void updateOrderItemStatus(@PathVariable("orderNumber") String orderNumber,
                               @RequestBody OrderItemStatusUpdateDto req);

    @PatchMapping("/api/admin/orders/{orderNumber}/cancel")
    void cancelOrder(@PathVariable("orderNumber") String orderNumber);
}
