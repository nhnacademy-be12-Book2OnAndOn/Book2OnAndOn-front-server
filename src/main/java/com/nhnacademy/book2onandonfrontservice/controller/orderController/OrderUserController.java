package com.nhnacademy.book2onandonfrontservice.controller.orderController;

import com.nhnacademy.book2onandonfrontservice.client.OrderUserClient;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderCreateRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderPrepareRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderCreateResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderDetailResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderPrepareResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderSimpleDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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

    // 장바구니 혹은 바로구매시 준비할 데이터 (책 정보, 회원 배송지 정보)
    @PostMapping("/prepare")
    public String getOrderPrepare(Model model,
                                  @CookieValue(value = "accessToken", required = false) String accessToken,
                                  @ModelAttribute  @RequestBody OrderPrepareRequestDto req){
        log.info("POST /orders/prepare 호출");

        // TODO err 페이지
        if(accessToken == null){
            return null;
        }

        String token = accessToken.startsWith("Bearer ") ? accessToken : "Bearer " + accessToken;

        OrderPrepareResponseDto orderSheetResponseDto = orderUserClient.getOrderPrepare(token, req);

        model.addAttribute("orderItems", orderSheetResponseDto.orderItems());
        model.addAttribute("addresses", orderSheetResponseDto.addresses());
        model.addAttribute("coupons", orderSheetResponseDto.coupons());
        model.addAttribute("point", orderSheetResponseDto.currentPoint());

        return "orderpayment/OrderPayment";
    }

    @PostMapping
    @ResponseBody
    public OrderCreateResponseDto createPreOrder(@CookieValue(value = "accessToken", required = false) String accessToken,
                                                 @RequestBody OrderCreateRequestDto req){
        log.info("POST /orders 호출 : 사전 주문 데이터 생성");

        // TODO err
        if(accessToken == null){
            return null;
        }

        String token = accessToken.startsWith("Bearer ") ? accessToken : "Bearer " + accessToken;

        return orderUserClient.createPreOrder(token, req);
    }

    // 주문조회 리스트 반환
    @GetMapping("/my-order")
    public String getOrderList(Model model,
                               @CookieValue(value = "accessToken", required = false) String accessToken,
                               Pageable pageable){
        log.info("GET /orders/my-order 호출 : 주문 리스트 데이터 반환");

        // TODO err 페이지
        if(accessToken == null){
            return null;
        }

        String token = accessToken.startsWith("Bearer ") ? accessToken : "Bearer " + accessToken;

        Page<OrderSimpleDto> page = orderUserClient.getOrderList(token, pageable);

        model.addAttribute("orderList", page);

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

        String token = accessToken.startsWith("Bearer ") ? accessToken : "Bearer " + accessToken;

        OrderDetailResponseDto orderResponseDto = orderUserClient.getOrderDetail(token, null, orderNumber);

        model.addAttribute("orderInfo", orderResponseDto);

        // TODO 주문 상세 내역 만들기
        return "";
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

        String token = accessToken.startsWith("Bearer ") ? accessToken : "Bearer " + accessToken;

        orderUserClient.cancelOrder(token, orderNumber);

        return "";
    }
}
