package com.nhnacademy.book2onandonfrontservice.controller.adminController;

import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.client.CouponClient;
import com.nhnacademy.book2onandonfrontservice.client.DeliveryClient;
import com.nhnacademy.book2onandonfrontservice.client.DeliveryPolicyClient;
import com.nhnacademy.book2onandonfrontservice.client.PointAdminClient;
import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.client.UserGradeClient;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookSaveRequest;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookStatus;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookStatusUpdateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookUpdateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.CategoryDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponDto.CouponDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponDto.CouponUpdateDto;
import com.nhnacademy.book2onandonfrontservice.dto.deliveryDto.DeliveryCompany;
import com.nhnacademy.book2onandonfrontservice.dto.deliveryDto.DeliveryDto;
import com.nhnacademy.book2onandonfrontservice.dto.deliveryDto.DeliveryPolicyDto;
import com.nhnacademy.book2onandonfrontservice.dto.deliveryDto.DeliveryWaybillUpdateDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.OrderStatus;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminViewController {

    private final UserClient userClient;
    private final CouponClient couponClient;
    private final BookClient bookClient;
    private final UserGradeClient userGradeClient;
    private final DeliveryClient deliveryClient;
    private final DeliveryPolicyClient deliveryPolicyClient;
    private final PointAdminClient pointAdminClient;

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

    //회원 상세 페이지
    @GetMapping("/users/{userId}")
    public String userDetail(HttpServletRequest request,
                             @PathVariable Long userId,
                             Model model) {

        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");

        try {
            UserResponseDto user = userClient.getUserDetail(token, userId);
            model.addAttribute("user", user);
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

    @PostMapping("/coupons/{couponId}/update-quantity")
    public String updateQuantity(HttpServletRequest request,
                                 @PathVariable("couponId") Long couponId,
                                 @RequestParam(required = false) Integer quantity) {

        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");
        CouponUpdateDto updateDto = new CouponUpdateDto(quantity);
        couponClient.updateCouponQuantity(token, couponId, updateDto);

        return "redirect:/admin/coupons";
    }

    /// --------------------------Book Admin ------------------------------------

    /// 도서 등록
    @PostMapping("/books/create")

    public String createBook(HttpServletRequest request, @ModelAttribute(value = "book") BookSaveRequest req,
                             @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        log.info("북서비스 등록 BookSaveRequest: {}", req.toString());
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");

        bookClient.createBook(req, images);
        return "redirect:/admin/books";
    }

    /// 도서 등록 페이지
    @GetMapping("/books/create")
    public String bookCreateForm(Model model) {
        List<CategoryDto> categories = bookClient.getCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("statuses", BookStatus.values());
        return "admin/books/create";
    }

    /// 도서 수정
    @PutMapping("/books/{bookId}")
    public String updateBook(HttpServletRequest request,
                             @ModelAttribute BookUpdateRequest req,
                             @PathVariable Long bookId,
                             @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");
        bookClient.updateBook(token, bookId, req, images);
        return "redirect:/admin/books";
    }

    /// 도서 삭제
    @DeleteMapping("/books/{bookId}")
    public String deleteBook(HttpServletRequest request,
                             @PathVariable Long bookId) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");
        bookClient.deleteBook(token, bookId);
        return "redirect:/admin/books";
    }

    /// 도서 상태변경
    @PatchMapping("/books/{bookId}/status")
    public String updateStatus(HttpServletRequest req, @PathVariable Long bookId,
                               @RequestParam("status") BookStatus status) {
        BookStatusUpdateRequest request = new BookStatusUpdateRequest(status);
        String token = "Bearer " + CookieUtils.getCookieValue(req, "accessToken");

        bookClient.updateBookStatus(token, bookId, request);

        return "redirect:/admin/books";
    }

    ///  -------------------------- Grades Admin --------------------------------------

    // 등급 목록 조회 페이지
    @GetMapping("/grades")
    public String gradeList(HttpServletRequest request, Model model) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");
        List<UserGradeDto> grades = userGradeClient.getAllGrades(token);
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

        DeliveryPolicyDto policy = deliveryPolicyClient.getDeliveryPolicy(token, id);

        model.addAttribute("deliveryPolicyDto", policy);
        model.addAttribute("pageTitle", "배송 정책 수정");
        return "admin/delivery/form";
    }

    @PostMapping("/delivery-policies/{id}")
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
                                       @RequestParam Long userId,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       Model model) {
        if (accessToken == null) {
            return "redirect:/login";
        }
        page = Math.max(0, page);
        size = size <= 0 ? 10 : size;
        Page<PointHistoryResponseDto> historyPage = pointAdminClient.getUserPointHistory("Bearer " + accessToken,
                userId, page, size);
        CurrentPointResponseDto currentPoint = pointAdminClient.getUserCurrentPoint("Bearer " + accessToken, userId);

        model.addAttribute("userId", userId);
        model.addAttribute("currentPoint", currentPoint);
        model.addAttribute("histories", historyPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", historyPage.getTotalPages());

        return "/user/mypage/point-history-admin";
    }

    @PostMapping("/points/adjust")
    public String adjustUserPoint(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @ModelAttribute PointHistoryAdminAdjustRequestDto requestDto) {

        if (accessToken == null) {
            return "redirect:/login";
        }

        pointAdminClient.adjustPointByAdmin("Bearer " + accessToken, requestDto);

        return "redirect:/admin/points?userId=" + requestDto.getUserId();
    }
}
