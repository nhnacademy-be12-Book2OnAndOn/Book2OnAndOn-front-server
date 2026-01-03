package com.nhnacademy.book2onandonfrontservice.controller.orderController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nhnacademy.book2onandonfrontservice.client.GuestOrderClient;
import com.nhnacademy.book2onandonfrontservice.client.OrderUserClient;
import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderCreateWrapperRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.OrderPrepareRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderCreateResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderDetailResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderPrepareResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderSimpleDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.RestPage;
import com.nhnacademy.book2onandonfrontservice.service.FrontTokenService;
import com.nhnacademy.book2onandonfrontservice.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
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
    private final GuestOrderClient guestOrderClient;
    private final OrderUserClient orderUserClient;
    private final UserClient userClient;
    private final FrontTokenService frontTokenService;

    // 장바구니 혹은 바로구매시 준비할 데이터 (책 정보, 회원 배송지 정보)
    @PostMapping("/prepare")
    public String getOrderPrepare(Model model,
                                  @CookieValue(value = "accessToken", required = false) String accessToken,
                                  @CookieValue(value = "GUEST_ID", required = false) String guestId, // 하나의 컨트롤러에서 처리하는 용도
                                  @ModelAttribute OrderPrepareRequestDto req) {
        log.info("POST /orders/prepare 호출");

        String token = toBearer(accessToken);

        OrderPrepareResponseDto orderPrepareResponseDto = null;
        try{
            if(accessToken != null){
                orderPrepareResponseDto = orderUserClient.getOrderPrepare(token, req);
            }else if(guestId != null){
                orderPrepareResponseDto = guestOrderClient.getOrderPrepare(guestId, req);
            }
        } catch (Exception e) {
            log.error("주문 페이지 이동 실패 {}", e.getMessage());
            return "redirect:/";
        }


//        try {
//            if (token == null) {
//                if (guestId == null || guestId.isBlank()) {
//                    guestId = CookieUtils.getCookieValue(request, "guestId");
//                }
//                if (guestId == null || guestId.isBlank()) {
//                    return "redirect:/cartpage?error=guest_id_missing";
//                }
//                orderPrepareResponseDto = guestOrderClient.getOrderPrepare(guestId, req);
//            } else {
//                orderPrepareResponseDto = orderUserClient.getOrderPrepare(token, guestId, userHeader, req);
//            }
//        } catch (FeignException.Unauthorized e) {
//            // 토큰이 만료/무효인 경우 게스트로 전환하여 다시 시도
//            log.warn("주문 준비 401 -> 게스트로 재시도");
//            frontTokenService.clearTokens();
//            token = null;
//            userHeader = null;
//            try {
//                if (guestId == null || guestId.isBlank()) {
//                    guestId = CookieUtils.getCookieValue(request, "guestId");
//                }
//                orderPrepareResponseDto = guestOrderClient.getOrderPrepare(guestId, req);
//            } catch (Exception ex) {
//                log.warn("주문 준비 게스트 재시도 실패", ex);
//                return "redirect:/cartpage?error=order_prepare_failed";
//            }
//        } catch (Exception e) {
//            log.warn("주문 준비 데이터 조회 실패", e);
//            return "redirect:/cartpage?error=order_prepare_failed";
//        }
//
//        // 헤더/뷰 공통 데이터
//        if (token != null) {
//            try {
//                model.addAttribute("user", userClient.getMyInfo(token));
//            } catch (Exception e) {
//                model.addAttribute("user", null);
//                log.warn("사용자 정보 조회 실패: {}", e.getMessage());
//            }
//        } else {
//            model.addAttribute("user", null);
//        }
//        model.addAttribute("cartCount",
//                request.getSession(false) != null ? request.getSession(false).getAttribute("cartCount") : null);

        boolean isGuest = (token == null);
        model.addAttribute("isGuest", isGuest);

        model.addAttribute("orderItems", orderPrepareResponseDto.orderItems());
        model.addAttribute("addresses", orderPrepareResponseDto.addresses());
        model.addAttribute("coupons", orderPrepareResponseDto.coupons());
        model.addAttribute("point", orderPrepareResponseDto.currentPoint());
        long itemTotal = orderPrepareResponseDto.orderItems() == null ? 0L :
                orderPrepareResponseDto.orderItems().stream()
                        .mapToLong(i -> {
                            long price = i.getPriceSales() != null ? i.getPriceSales()
                                    : (i.getPriceStandard() != null ? i.getPriceStandard() : 0L);
                            long qty = i.getQuantity() != null ? i.getQuantity() : 1L;
                            return price * qty;
                        }).sum();
        model.addAttribute("itemTotal", itemTotal);

        return "orderpayment/OrderPayment";
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<OrderCreateResponseDto> createPreOrder(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @CookieValue(value = "GUEST_ID", required = false) String guestId,
            @RequestBody OrderCreateWrapperRequestDto req) {
        log.info("POST /orders 호출 : 사전 주문 데이터 생성");

        String token = toBearer(accessToken);

        OrderCreateResponseDto orderCreateResponseDto = null;

        log.info("비밀번호 왜 안찌혀 : {}", req.guest().getGuestPassword());

        if(token == null || token.isEmpty()){
            orderCreateResponseDto = guestOrderClient.createGuestOrder(guestId, req.guest());
        }else{
            orderCreateResponseDto = orderUserClient.createPreOrder(token, req.user());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(orderCreateResponseDto);
    }

    // 주문조회 리스트 반환
    @GetMapping("/my-order")
    public String getOrderList(Model model,
                               @CookieValue(value = "accessToken", required = false) String accessToken,
                               @PageableDefault(size = 3, sort = "orderDateTime", direction = Sort.Direction.DESC) Pageable pageable,
                               HttpServletRequest request) {
        log.info("GET /orders/my-order 호출 : 주문 리스트 데이터 반환");

        // TODO err 페이지
        if (accessToken == null) {
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
//        Object cartCount =
//                request.getSession(false) != null ? request.getSession(false).getAttribute("cartCount") : null;
//        model.addAttribute("cartCount", cartCount);
        model.addAttribute("orderList", page);
        List<Integer> pageNumbers = page.getTotalPages() > 0
                ? IntStream.rangeClosed(1, page.getTotalPages()).boxed().toList()
                : List.of();
        model.addAttribute("pageNumbers", pageNumbers);

        return "orderpayment/OrderHistory";

    }

    @GetMapping("/{orderNumber}")
    @ResponseBody
    public OrderDetailResponseDto getOrderDetail(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @PathVariable("orderNumber") String orderNumber) {
        log.info("GET /orders/{} 호출 : 주문 상세 데이터 반환", orderNumber);

        if (accessToken == null) {
            return null;
        }

        String token = toBearer(accessToken);

        return orderUserClient.getOrderDetail(token, null, orderNumber);
    }

    /**
     * 주문 상세 개별 페이지 뷰 (리스트 내 인라인 상세와 별도)
     */
    @GetMapping("/{orderNumber}/page")
    public String orderDetailPage(Model model,
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
        Object cartCount =
                request.getSession(false) != null ? request.getSession(false).getAttribute("cartCount") : null;
        model.addAttribute("cartCount", cartCount);
        model.addAttribute("order", order);

        return "orderpayment/OrderDetail";
    }

    @GetMapping("/complete/{orderNumber}")
    public String orderComplete(Model model,
                                @CookieValue(value = "accessToken", required = false) String accessToken,
                                @PathVariable("orderNumber") String orderNumber,
                                HttpServletRequest request) {
        if (accessToken == null) {
            return "redirect:/";
        }
        String token = toBearer(accessToken);
        OrderDetailResponseDto order = orderUserClient.getOrderDetail(token, null, orderNumber);
        try {
            model.addAttribute("user", userClient.getMyInfo(token));
        } catch (Exception e) {
            model.addAttribute("user", null);
        }
        Object cartCount =
                request.getSession(false) != null ? request.getSession(false).getAttribute("cartCount") : null;
        model.addAttribute("cartCount", cartCount);
        model.addAttribute("order", order);
        return "orderpayment/OrderComplete";
    }

    @GetMapping("/{orderNumber}/cancel")
    public String cancelOrder(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestHeader(value = "X-Guest-Order-Token", required = false) String guestToken,
            @PathVariable("orderNumber") String orderNumber
    ) {
        log.info("GET /orders/{}/cancel 호출 : 주문 취소", orderNumber);

        if (accessToken == null) {
            // 비회원
            orderUserClient.cancelOrder(null, guestToken, orderNumber);
        } else {
            // 회원
            String token = toBearer(accessToken);
            orderUserClient.cancelOrder(token, null, orderNumber);
        }

        // redirect
        return "redirect:/orders/" + orderNumber + "/page";
    }


    private String toBearer(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return null;
        }
        String decoded = accessToken;
        try {
            decoded = URLDecoder.decode(accessToken, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }
        return decoded.startsWith("Bearer ") ? decoded : "Bearer " + decoded;
    }

    private Long resolveUserId(String accessToken) {
        try {
            return JwtUtils.getUserId(accessToken);
        } catch (Exception e) {
            return null;
        }
    }

    private RestPage<OrderSimpleDto> toRestPage(Map<String, Object> raw, Pageable pageable) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        var content = mapper.convertValue(raw.getOrDefault("content", List.of()),
                new TypeReference<List<OrderSimpleDto>>() {
                });
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
