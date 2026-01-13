package com.nhnacademy.book2onandonfrontservice.controller.adminController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.client.BookReindexClient;
import com.nhnacademy.book2onandonfrontservice.client.CouponClient;
import com.nhnacademy.book2onandonfrontservice.client.DeliveryClient;
import com.nhnacademy.book2onandonfrontservice.client.DeliveryPolicyClient;
import com.nhnacademy.book2onandonfrontservice.client.OrderUserClient;
import com.nhnacademy.book2onandonfrontservice.client.PointAdminClient;
import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.client.UserGradeClient;
import com.nhnacademy.book2onandonfrontservice.dto.couponDto.CouponDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponDto.CouponUpdateDto;
import com.nhnacademy.book2onandonfrontservice.dto.deliveryDto.DeliveryCompany;
import com.nhnacademy.book2onandonfrontservice.dto.deliveryDto.DeliveryDto;
import com.nhnacademy.book2onandonfrontservice.dto.deliveryDto.DeliveryPolicyDto;
import com.nhnacademy.book2onandonfrontservice.dto.deliveryDto.DeliveryWaybillUpdateDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.OrderSimpleDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.status.OrderStatus;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.CurrentPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointHistoryAdminAdjustRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointHistoryResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.RestPage;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.UserGradeDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.AdminUserUpdateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.UserGradeRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.UserResponseDto;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminViewController {

    private final UserClient userClient;
    private final CouponClient couponClient;
    private final BookClient bookClient;
    private final BookReindexClient bookReindexClient;
    private final UserGradeClient userGradeClient;
    private final DeliveryClient deliveryClient;
    private final DeliveryPolicyClient deliveryPolicyClient;
    private final PointAdminClient pointAdminClient;
    private final OrderUserClient orderUserClient;


    //관리자 대시보드
    @GetMapping
    public String dashboard(HttpServletRequest request, Model model) {
        String accessToken = CookieUtils.getCookieValue(request, "accessToken");
        if (accessToken == null) {
            return "redirect:/login";
        }
        String token = "Bearer " + accessToken; // "Bearer " 접두어 추가

        try {
            ResponseEntity<Long> response = userClient.getUserCount(token);
            model.addAttribute("totalUserCount", response.getBody());
        } catch (Exception e) {
            log.error("대시보드 회원 수 조회 실패", e);
            model.addAttribute("totalUserCount", 0);
        }

        try {
            Long count = bookClient.countAllBook(token);
            model.addAttribute("bookCount", count);
        } catch (Exception e) {
            log.error("대시보드 도서 수 조회 실패", e);
            model.addAttribute("bookCount", 0);
        }

        // 기본 지표 값 초기화 (추후 API 연동 시 교체)
        model.addAttribute("todayOrderCount", 0);
        model.addAttribute("newReviewCount", 0);
        model.addAttribute("totalSalesAmount", 0);
        model.addAttribute("totalOrderCount", 0);
        return "admin/index";
    }

    //회원 관리 페이지
    @GetMapping("/users")
    public String userList(HttpServletRequest request, Model model,
                           @RequestParam(defaultValue = "0") int page) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");

        try {
            RestPage<UserResponseDto> userPage = userClient.getUsers(token, page, 10);

            model.addAttribute("users", userPage.getContent());
            model.addAttribute("page", userPage);

        } catch (Exception e) {
            log.error("회원 목록 조회 실패", e);
            model.addAttribute("error", "회원 목록을 불러오지 못했습니다.");
        }
        return "admin/users";
    }

    // 주문 관리 페이지
//    @GetMapping("/orders")
//    public String orderList(HttpServletRequest request, Model model,
//                            @RequestParam(defaultValue = "0") int page,
//                            @RequestParam(defaultValue = "10") int size) {
//        // TODO: 주문 관리자 API 연동 후 페이지네이션 적용
//        model.addAttribute("orders", java.util.List.of());
//        model.addAttribute("page", null);
//        return "admin/orders";
//    }
//    @GetMapping("/orders")
//    public String orderList(HttpServletRequest request, Model model,
//                            @RequestParam(defaultValue = "0") int page,
//                            @RequestParam(defaultValue = "10") int size) {
//
//        // 0) 진입 로그 (이게 안 찍히면 매핑이 다른 컨트롤러로 타는 겁니다)
//        log.info("[ADMIN-ORDERS] enter /admin/orders page={}, size={}", page, size);
//
//        // 1) 토큰 확인
//        String accessToken = CookieUtils.getCookieValue(request, "accessToken");
//        if (accessToken == null || accessToken.isBlank()) {
//            log.warn("[ADMIN-ORDERS] no accessToken cookie -> redirect login");
//            return "redirect:/login";
//        }
//        String token = accessToken.startsWith("Bearer ") ? accessToken : "Bearer " + accessToken;
//
//        // 2) 헤더에서 쓰는 값들
//        Object cartCount = request.getSession(false) != null
//                ? request.getSession(false).getAttribute("cartCount")
//                : null;
//        model.addAttribute("cartCount", cartCount);
//
//        try {
//            model.addAttribute("user", userClient.getMyInfo(token));
//        } catch (Exception e) {
//            log.warn("[ADMIN-ORDERS] /api/users/me failed (header user). continue.", e);
//            model.addAttribute("user", null);
//        }
//
//        // 3) page/size 안전화
//        page = Math.max(0, page);
//        size = (size <= 0) ? 10 : size;
//
//        try {
//            // 4) 주문 목록 호출 (기존 my-order 재사용)
//            var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDateTime"));
//
//            // OrderUserClient가 Map 형태로 받는다고 가정 (toRestPage 없이 처리)
//            Map<String, Object> raw = orderUserClient.getOrderList(token, pageable);
//
//            log.info("[ADMIN-ORDERS] raw keys={}", raw != null ? raw.keySet() : null);
//
//            ObjectMapper mapper = new ObjectMapper();
//            mapper.registerModule(new JavaTimeModule());
//
//            // 5) content -> List<OrderSimpleDto>
//            List<OrderSimpleDto> orders = mapper.convertValue(
//                    raw.getOrDefault("content", List.of()),
//                    new TypeReference<List<OrderSimpleDto>>() {}
//            );
//
//            log.info("[ADMIN-ORDERS] orders size={}", orders != null ? orders.size() : null);
//
//            model.addAttribute("orders", orders);
//
//            // (선택) 페이지 정보도 같이 내려줌. 템플릿에서 안 쓰면 지워도 무방
//            Object totalPagesObj = raw.get("totalPages");
//            Object totalElementsObj = raw.get("totalElements");
//
//            int totalPages = (totalPagesObj instanceof Number n) ? n.intValue() : 0;
//            long totalElements = (totalElementsObj instanceof Number n) ? n.longValue() : (orders != null ? orders.size() : 0);
//
//            model.addAttribute("currentPage", page);
//            model.addAttribute("pageSize", size);
//            model.addAttribute("totalPages", totalPages);
//            model.addAttribute("totalElements", totalElements);
//
//        } catch (Exception e) {
//            // 여기로 오면 화면에 "error"가 떠야 정상
//            log.error("[ADMIN-ORDERS] load orders failed", e);
//            model.addAttribute("orders", List.of());
//            model.addAttribute("error", "주문 목록을 불러오지 못했습니다.");
//        }
//
//        return "admin/orders";
//    }

    //회원 상세 페이지
    @GetMapping("/users/{userId}")
    public String userDetail(HttpServletRequest request,
                             @PathVariable Long userId,
                             Model model) {

        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");

        try {
            UserResponseDto targetUser = userClient.getUserDetail(token, userId);
            model.addAttribute("targetUser", targetUser);
            try {
                CurrentPointResponseDto currentPoint =
                        pointAdminClient.getUserCurrentPoint(token, userId);
                model.addAttribute("currentPoint", currentPoint);
            } catch (Exception e) {
                log.warn("회원 현재 포인트 조회 실패 (userId={})", userId, e);
            }
            return "admin/user-detail";
        } catch (Exception e) {
            return "redirect:/admin/users?error=not_found";
        }
    }

    //회원 정보 수정
    @PostMapping("/users/{userId}/update")
    public String updateUser(HttpServletRequest request,
                             @PathVariable Long userId,
                             @ModelAttribute AdminUserUpdateRequest updateRequest) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");

        try {
            userClient.updateUser(token, userId, updateRequest);
            return "redirect:/admin/users?success=update";
        } catch (Exception e) {
            log.error("회원 수정 실패", e);
            return "redirect:/admin/users?error=update_failed";
        }
    }

    //회원 강제 탈퇴
    @PostMapping("/users/{userId}/delete")
    public String deleteUser(HttpServletRequest request,
                             @PathVariable Long userId,
                             @RequestParam String reason) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");

        try {
            userClient.deleteUser(token, userId, reason);
            return "redirect:/admin/users?success=delete";
        } catch (Exception e) {
            log.error("회원 삭제 실패", e);
            return "redirect:/admin/users?error=delete_failed";
        }
    }

    @GetMapping("/coupons")
    public String listCoupons(HttpServletRequest request,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String status,
                              Model model) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");

        Page<CouponDto> couponPage = couponClient.getCoupons(token, page, size, status);

        model.addAttribute("coupons", couponPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPage", couponPage.getTotalPages());
        model.addAttribute("searchStatus", status);
        model.addAttribute("today", LocalDate.now());

        return "admin/coupon/list";
    }

    @PostMapping("/coupons/{coupon-id}/update-quantity")
    public String updateQuantity(HttpServletRequest request,
                                 @PathVariable("coupon-id") Long couponId,
                                 @RequestParam(required = false) Integer quantity) {

        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");
        CouponUpdateDto updateDto = new CouponUpdateDto(quantity);
        couponClient.updateCouponQuantity(token, couponId, updateDto);

        return "redirect:/admin/coupons";
    }


    ///  -------------------------- Grades Admin --------------------------------------

    // 등급 목록 조회 페이지
    @GetMapping("/grades")
    public String gradeList(HttpServletRequest request, Model model) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");
        List<UserGradeDto> grades = userGradeClient.getAllGrades();
        model.addAttribute("grades", grades);
        return "admin/grades/list";
    }

    // 새 등급 생성
    @PostMapping("/grades")
    public String createGrade(HttpServletRequest req, @ModelAttribute UserGradeRequestDto request) {
        String token = "Bearer " + CookieUtils.getCookieValue(req, "accessToken");
        userGradeClient.createGrade(token, request);
        return "redirect:/admin/grades";
    }

    // 등급 정보 수정
    @PostMapping("/grades/{gradeId}/update")
    public String updateGrade(HttpServletRequest req, @PathVariable Long gradeId,
                              @ModelAttribute UserGradeRequestDto request) {
        String token = "Bearer " + CookieUtils.getCookieValue(req, "accessToken");
        userGradeClient.updateGrade(token, gradeId, request);
        return "redirect:/admin/grades";
    }

    ///  -------------------------- Deliveries Admin --------------------------------------

    @GetMapping("/deliveries")
    public String listDeliveries(HttpServletRequest request,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) OrderStatus status,
                                 Model model) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");

        Page<DeliveryDto> deliveryPage = deliveryClient.getDeliveries(token, page, size, status);
        log.info("배송: {}", deliveryPage.getTotalElements());
        model.addAttribute("deliveries", deliveryPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", deliveryPage.getTotalPages());
        model.addAttribute("searchStatus", status);

        // 필터용 Enum 데이터 (주문 상태, 택배사 목록)
        model.addAttribute("orderStatuses", OrderStatus.values());
        model.addAttribute("deliveryCompanies", DeliveryCompany.values());

        // 페이지네이션 범위 계산
        int startPage = Math.max(0, page - 2);
        int endPage = Math.min(Math.max(0, deliveryPage.getTotalPages() - 1), page + 2);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "admin/delivery/list"; // templates/admin/delivery/list.html
    }

    //운송장 등록 처리 (배송 시작)
    @PostMapping("/deliveries/{deliveryId}/waybill")
    public String registerWaybill(HttpServletRequest request,
                                  @PathVariable Long deliveryId,
                                  @ModelAttribute DeliveryWaybillUpdateDto requestDto) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");

        deliveryClient.registerWaybill(token, deliveryId, requestDto);

        return "redirect:/admin/deliveries?status=PREPARING";
    }


    //운송장 정보 수정 처리
    @PostMapping("/deliveries/{deliveryId}/info")
    public String updateDeliveryInfo(HttpServletRequest request,
                                     @PathVariable Long deliveryId,
                                     @ModelAttribute DeliveryWaybillUpdateDto requestDto) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");

        deliveryClient.updateDeliveryInfo(token, deliveryId, requestDto);

        return "redirect:/admin/deliveries";
    }

    @GetMapping("/delivery-policies")
    public String getDeliveries(HttpServletRequest request,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");

        Page<DeliveryPolicyDto> deliveryPolicyPage = deliveryPolicyClient.getDeliveryPolicies(token, page, size);
        log.info("배송 정책 조회: {}", deliveryPolicyPage.getTotalElements());
        model.addAttribute("deliveryPolicies", deliveryPolicyPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", deliveryPolicyPage.getTotalPages());

        // 페이지네이션 범위 계산
        int startPage = Math.max(0, page - 2);
        int endPage = Math.min(Math.max(0, deliveryPolicyPage.getTotalPages() - 1), page + 2);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "admin/delivery/policy-list";
    }

    @PostMapping("/delivery-policies")
    public String createDeliveries(HttpServletRequest request,
                                   @ModelAttribute DeliveryPolicyDto dto) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");

        log.info("배송 정책 요청 값: {}", dto);
        deliveryPolicyClient.createDeliveryPolicy(token, dto);
        log.info("deliveryPolicyName = {}", dto.getDeliveryPolicyName());
        log.info("deliveryFee = {}", dto.getDeliveryFee());
        log.info("freeDeliveryThreshold = {}", dto.getFreeDeliveryThreshold());

        return "redirect:/admin/delivery-policies";
    }

    @GetMapping("/delivery-policies/create")
    public String createForm(Model model) {
        model.addAttribute("deliveryPolicyDto", new DeliveryPolicyDto());
        model.addAttribute("pageTitle", "배송 정책 등록");
        return "admin/delivery/form";
    }

    @GetMapping("/delivery-policies/update/{id}")
    public String updateForm(HttpServletRequest request, @PathVariable Long id, Model model) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");
        try {
            Page<DeliveryPolicyDto> page = deliveryPolicyClient.getDeliveryPolicies(token, 0, 1000);
            DeliveryPolicyDto policy = page.getContent().stream()
                    .filter(item -> item.getDeliveryPolicyId() != null && item.getDeliveryPolicyId().equals(id))
                    .findFirst()
                    .orElse(null);

            if (policy == null) {
                return "redirect:/admin/delivery-policies?error=not_found";
            }

            model.addAttribute("deliveryPolicyDto", policy);
            model.addAttribute("pageTitle", "배송 정책 수정");
            return "admin/delivery/form";
        } catch (Exception e) {
            return "redirect:/admin/delivery-policies?error=load_failed";
        }
    }

    @PutMapping("/delivery-policies/{id}")
    public String updateDeliveryPolicy(
            HttpServletRequest request,
            @PathVariable Long id,
            @ModelAttribute DeliveryPolicyDto dto) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");

        deliveryPolicyClient.updateDeliveryPolicy(token, id, dto);
        return "redirect:/admin/delivery-policies";
    }

    ///  -------------------------- Point Admin --------------------------------------

    @GetMapping("/points")
    public String listUserPointHistory(@CookieValue(value = "accessToken", required = false) String accessToken,
                                       @RequestParam(required = false) Long userId,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       Model model) {
        if (accessToken == null) {
            return "redirect:/login";
        }
        if (userId != null) {
            page = Math.max(0, page);
            size = size <= 0 ? 10 : size;
            try {
                Page<PointHistoryResponseDto> historyPage = pointAdminClient.getUserPointHistory("Bearer " + accessToken,
                        userId, page, size);
                CurrentPointResponseDto currentPoint = pointAdminClient.getUserCurrentPoint("Bearer " + accessToken, userId);

                model.addAttribute("currentPoint", currentPoint);
                model.addAttribute("histories", historyPage.getContent());
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", historyPage.getTotalPages());
            } catch (Exception e) {
                log.warn("포인트 이력 조회 실패 userId={}", userId, e);
                model.addAttribute("errorMessage", "포인트 정보를 불러오지 못했습니다.");
                model.addAttribute("histories", List.of());
                model.addAttribute("currentPage", 0);
                model.addAttribute("totalPages", 0);
            }
            model.addAttribute("userId", userId);
        }
        return "admin/point-history-admin";
    }

    @PostMapping("/points/adjust")
    public String adjustUserPoint(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @ModelAttribute PointHistoryAdminAdjustRequestDto requestDto) {

        if (accessToken == null) {
            return "redirect:/login";
        }

        try {
            pointAdminClient.adjustPointByAdmin("Bearer " + accessToken, requestDto);
            return "redirect:/admin/points?userId=" + requestDto.getUserId();
        } catch (Exception e) {
            log.warn("포인트 조정 실패 userId={}", requestDto.getUserId(), e);
            return "redirect:/admin/points?userId=" + requestDto.getUserId() + "&error=adjust_failed";
        }
    }
}
