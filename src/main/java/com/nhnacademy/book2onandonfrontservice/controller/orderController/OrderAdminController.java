package com.nhnacademy.book2onandonfrontservice.controller.orderController;

import com.nhnacademy.book2onandonfrontservice.client.OrderAdminClient;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderItemStatusUpdateDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderStatusUpdateDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderDetailResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderSimpleDto;

import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.PageResponse;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.status.OrderItemStatus;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.status.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/orders")
public class OrderAdminController {

    private final OrderAdminClient orderAdminClient;

    @GetMapping
    public String getOrderList(
            @CookieValue(name = "accessToken", required = false) String accessToken,
            @RequestParam(value = "page", defaultValue = "1") int page, // 1-based
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        log.info("GET /admin/orders : 관리자 주문 리스트 요청 page = {}, size = {}", page, size);

        if (accessToken == null || accessToken.isEmpty()) {
            return "redirect:/login";
        }

        // 서버에서는 0-based로 변환
        int zeroBasedPage = Math.max(page - 1, 0);

        Pageable safePageable = PageRequest.of(
                zeroBasedPage,
                size <= 0 ? 10 : size,
                Sort.by(Sort.Direction.DESC, "orderDateTime")
        );

        PageResponse<OrderSimpleDto> response = orderAdminClient.getOrderList(safePageable);

        model.addAttribute("orderList", response.getContent());
        model.addAttribute("currentPage", page); // 화면에 1-based로 출력
        model.addAttribute("pageSize", response.getSize());
        model.addAttribute("totalPages", response.getTotalPages());
        model.addAttribute("totalElements", response.getTotalElements());
        model.addAttribute("last", response.isLast());

        return "admin/orders";
    }

    @GetMapping("/{orderNumber}")
    public String getOrderDetail(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @PathVariable("orderNumber") String orderNumber,
            Model model
    ) {
        log.info("GET /admin/orders/{} 호출 : 관리자 주문 상세 페이지", orderNumber);

        if (accessToken == null || accessToken.isBlank()) {
            return "redirect:/login";
        }

        // 관리자 주문 상세 조회
        OrderDetailResponseDto order =
                orderAdminClient.getOrderDetail(orderNumber);

        model.addAttribute("order", order);

        // 상태 변경용 enum 리스트
        model.addAttribute("orderStatusList", OrderStatus.values());
        model.addAttribute("orderItemStatusList", OrderItemStatus.values());

        return "admin/order/order-detail"; // admin-order-detail.html
    }

    @PatchMapping("/{orderNumber}")
    public void updateOrderStatus(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @PathVariable("orderNumber") String orderNumber,
            @RequestBody OrderStatusUpdateDto req
    ) {
        log.info("PATCH /admin/orders/{} : 주문 상태 변경 요청 {}", orderNumber, req);

        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("관리자 인증 정보 없음");
        }

        orderAdminClient.updateOrderStatus(orderNumber, req);
    }

    @PatchMapping("/{orderNumber}/order-items")
    public void updateOrderItemStatus(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @PathVariable("orderNumber") String orderNumber,
            @RequestBody OrderItemStatusUpdateDto req
    ) {
        log.info("PATCH /admin/orders/{}/order-items : 주문 상품 상태 변경 {}", orderNumber, req);

        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("관리자 인증 정보 없음");
        }

        orderAdminClient.updateOrderItemStatus(orderNumber, req);
    }

    @GetMapping("/{orderNumber}/cancel")
    public String cancelOrder(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @PathVariable String orderNumber
    ) {
        log.info("GET /admin/orders/{}/cancel : 관리자 주문 취소", orderNumber);

        if (accessToken == null || accessToken.isBlank()) {
            return "redirect:/login";
        }

        orderAdminClient.cancelOrder(orderNumber);

        return "redirect:/admin/orders/" + orderNumber;
    }
}
