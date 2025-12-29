package com.nhnacademy.book2onandonfrontservice.controller.orderController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nhnacademy.book2onandonfrontservice.client.OrderUserClient;
import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderCreateRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderPrepareRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.MemberCouponResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderCreateResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderDetailResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderPrepareResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderSimpleDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.RestPage;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderUserController {
    private final OrderUserClient orderUserClient;
    private final UserClient userClient;

    // 장바구니 혹은 바로구매시 준비할 데이터 (책 정보, 회원 배송지 정보)
    @PostMapping("/prepare")
    public String getOrderPrepare(Model model,
                                  @CookieValue(value = "accessToken", required = false) String accessToken,
                                  @ModelAttribute OrderPrepareRequestDto req,
                                  HttpServletRequest request){
        log.info("POST /orders/prepare 호출");

        // TODO err 페이지
        if(accessToken == null){
            return "redirect:/login";
        }

        String token = toBearer(accessToken);

        OrderPrepareResponseDto orderSheetResponseDto;
        try {
            orderSheetResponseDto = orderUserClient.getOrderPrepare(token, req);
        } catch (Exception e) {
            log.warn("주문 준비 데이터 조회 실패", e);
            return "redirect:/cartpage?error=order_prepare_failed";
        }

        // 헤더/뷰 공통 데이터
        try {
            model.addAttribute("user", userClient.getMyInfo(token));
        } catch (Exception e) {
            model.addAttribute("user", null);
            log.warn("사용자 정보 조회 실패: {}", e.getMessage());
        }
        model.addAttribute("cartCount", request.getSession(false) != null ? request.getSession(false).getAttribute("cartCount") : null);

        model.addAttribute("orderItems", orderSheetResponseDto.orderItems());
        model.addAttribute("addresses", orderSheetResponseDto.addresses());
        model.addAttribute("coupons", orderSheetResponseDto.coupons());
        model.addAttribute("point", orderSheetResponseDto.currentPoint());
        long itemTotal = orderSheetResponseDto.orderItems() == null ? 0L :
                orderSheetResponseDto.orderItems().stream()
                        .mapToLong(i -> {
                            long price = i.getPriceSales() != null ? i.getPriceSales() : (i.getPriceStandard() != null ? i.getPriceStandard() : 0L);
                            long qty = i.getQuantity() != null ? i.getQuantity() : 1L;
                            return price * qty;
                        }).sum();
        model.addAttribute("itemTotal", itemTotal);

        return "orderpayment/OrderPayment";
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<OrderCreateResponseDto> createPreOrder(@CookieValue(value = "accessToken", required = false) String accessToken,
                                                                @RequestBody OrderCreateRequestDto req){
        log.info("POST /orders 호출 : 사전 주문 데이터 생성");

        // TODO err
        if(accessToken == null){
            return null;
        }

        String token = toBearer(accessToken);

        OrderCreateResponseDto orderCreateResponseDto = orderUserClient.createPreOrder(token, req);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderCreateResponseDto);
    }

    // 주문조회 리스트 반환
    @GetMapping("/my-order")
    public String getOrderList(Model model,
                               @CookieValue(value = "accessToken", required = false) String accessToken,
                               @PageableDefault(size = 3, sort = "orderDateTime", direction = Sort.Direction.DESC) Pageable pageable,
                               HttpServletRequest request){
        log.info("GET /orders/my-order 호출 : 주문 리스트 데이터 반환");

        // TODO err 페이지
        if(accessToken == null){
            return "redirect:/login";
        }

        String token = toBearer(accessToken);

        var raw = orderUserClient.getOrderList(token, pageable);
        RestPage<OrderSimpleDto> page = toRestPage(raw, pageable);

        try {
            model.addAttribute("user", userClient.getMyInfo(token));
        } catch (Exception e) {
            model.addAttribute("user", null);
            log.warn("사용자 정보 조회 실패: {}", e.getMessage());
        }
        Object cartCount = request.getSession(false) != null ? request.getSession(false).getAttribute("cartCount") : null;
        model.addAttribute("cartCount", cartCount);
        model.addAttribute("orderList", page);
        List<Integer> pageNumbers = page.getTotalPages() > 0
                ? IntStream.rangeClosed(1, page.getTotalPages()).boxed().toList()
                : List.of();
        model.addAttribute("pageNumbers", pageNumbers);

        return "orderpayment/OrderHistory";

    }

    @GetMapping("/{orderNumber}")
    public String getOrderDetail(Model model,
                                 @CookieValue(value = "accessToken", required = false) String accessToken,
                                 @PathVariable("orderNumber") String orderNumber){
        log.info("GET /orders/{} 호출 : 주문 상세 데이터 반환" , orderNumber);

        // TODO
        if(accessToken == null){
            return null;
        }

        String token = toBearer(accessToken);

        OrderDetailResponseDto orderResponseDto = orderUserClient.getOrderDetail(token, null, orderNumber);

        model.addAttribute("orderInfo", orderResponseDto);

        // TODO 주문 상세 내역 만들기
        return "";
    }

    @GetMapping("/complete/{orderNumber}")
    public String orderComplete(Model model,
                                @CookieValue(value = "accessToken", required = false) String accessToken,
                                @PathVariable("orderNumber") String orderNumber,
                                HttpServletRequest request) {
        if (accessToken == null) {
            return "redirect:/login";
        }
        String token = toBearer(accessToken);
        OrderDetailResponseDto order = orderUserClient.getOrderDetail(token, null, orderNumber);
        try {
            model.addAttribute("user", userClient.getMyInfo(token));
        } catch (Exception e) {
            model.addAttribute("user", null);
        }
        Object cartCount = request.getSession(false) != null ? request.getSession(false).getAttribute("cartCount") : null;
        model.addAttribute("cartCount", cartCount);
        model.addAttribute("order", order);
        return "orderpayment/OrderComplete";
    }

    // 결제 후 바로 주문 취소하는 경우
    @PatchMapping("/{orderNumber}/cancel")
    public String cancelOrder(@CookieValue(value = "accessToken", required = false) String accessToken,
                                            @PathVariable("orderNumber") String orderNumber){
        log.info("PATCH /orders/{}/cancel 호출 : 주문 취소", orderNumber);

        // TODO
        if(accessToken == null){
            return null;
        }

        String token = toBearer(accessToken);

        orderUserClient.cancelOrder(token, orderNumber);

        return "";
    }

    private String toBearer(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) return null;
        return accessToken.startsWith("Bearer ") ? accessToken : "Bearer " + accessToken;
    }

    private RestPage<OrderSimpleDto> toRestPage(java.util.Map<String, Object> raw, Pageable pageable) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        var content = mapper.convertValue(raw.getOrDefault("content", java.util.List.of()),
                new TypeReference<java.util.List<OrderSimpleDto>>() {});
        long total = 0;
        Object totalObj = raw.get("totalElements");
        if (totalObj instanceof Number n) {
            total = n.longValue();
        } else if (content != null) {
            total = content.size();
        }
        return new RestPage<>(content, pageable, total);
    }
}
