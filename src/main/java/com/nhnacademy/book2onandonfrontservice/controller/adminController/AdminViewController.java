package com.nhnacademy.book2onandonfrontservice.controller.adminController;

import com.nhnacademy.book2onandonfrontservice.client.CouponClient;
import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.dto.couponDto.CouponDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponDto.CouponUpdateDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.RestPage;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.AdminUserUpdateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.UserResponseDto;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminViewController {

    private final UserClient userClient;
    private final CouponClient couponClient;
//    private final UserGradeClient userGradeClient;

    //관리자 대시보드
    @GetMapping
    public String dashboard(HttpServletRequest request, Model model) {
        String accessToken = CookieUtils.getCookieValue(request, "accessToken");
        if (accessToken == null) {
            return "redirect:/login";
        }
        String token = "Bearer " + accessToken;

        try {
            RestPage<UserResponseDto> userPage = userClient.getUsers(token, 0, 1);
            model.addAttribute("totalUserCount", userPage.getTotalElements());
        } catch (Exception e) {
            log.error("대시보드 데이터 조회 실패 ", e);
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
    public String listCoupons(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String status,
                              Model model) {

        Page<CouponDto> couponPage = couponClient.getCoupons(page, size, status);

        model.addAttribute("coupons", couponPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPage", couponPage.getTotalPages());
        model.addAttribute("searchStatus", status);
        model.addAttribute("today", LocalDate.now());

        return "admin/coupon/list";
    }

    @PostMapping("/coupons/{couponId}/update-quantity")
    public String updateQuantity(@PathVariable("couponId") Long couponId,
                                 @RequestParam(required = false) Integer quantity) {

        CouponUpdateDto updateDto = new CouponUpdateDto(quantity);
        couponClient.updateCouponQuantity(couponId, updateDto);

        return "redirect:/admin/coupons";
    }

//    // 등급 목록 조회 페이지
//    @GetMapping("/grades")
//    public String gradeList(Model model) {
//        List<UserGradeDto> grades = userGradeClient.getAllGrades();
//        model.addAttribute("grades", grades);
//        return "admin/grades/list";
//    }
//
//    // 새 등급 생성
//    @PostMapping("/grades")
//    public String createGrade(@ModelAttribute UserGradeRequestDto request) {
//        userGradeClient.createGrade(request);
//        return "redirect:/admin/grades";
//    }
//
//    // 등급 정보 수정
//    @PostMapping("/grades/{gradeId}/update")
//    public String updateGrade(@PathVariable Long gradeId, @ModelAttribute UserGradeRequestDto request) {
//        userGradeClient.updateGrade(gradeId, request);
//        return "redirect:/admin/grades";
//    }
}