package com.nhnacademy.book2onandonfrontservice.controller.userController;

import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.RestPage;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.UserAddressCreateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.UserAddressUpdateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.UserUpdateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.BookReviewResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.UserAddressResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.UserResponseDto;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import com.nhnacademy.book2onandonfrontservice.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


//마이페이지 (accessToken 접근 테스트용 임시)
@Slf4j
@Controller
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class MyPageViewController {

    private final UserClient userClient;

    //마이페이지
    @GetMapping
    public String myPageHome(HttpServletRequest request, Model model) {
        // 1. 토큰 및 ID 추출
        String accessToken = CookieUtils.getCookieValue(request, "accessToken");
        Long myUserId = JwtUtils.getUserId(accessToken);

        if (myUserId == null) {
            return "redirect:/login"; // 로그인 안 했으면 튕겨냄
        }

        try {
            UserResponseDto myInfo = userClient.getMyInfo("Bearer " + accessToken);
            model.addAttribute("user", myInfo);

            try {
                RestPage<BookReviewResponseDto> reviewPage = userClient.getUserReviews(myUserId, 0, 3);
                model.addAttribute("recentReviews", reviewPage.getContent());
            } catch (Exception e) {
                log.warn("최근 리뷰 조회 실패: {}", e.getMessage());
                model.addAttribute("recentReviews", List.of());
            }

            model.addAttribute("couponCount", 0);
            model.addAttribute("orderCount", 0);

            return "user/mypage/index";

        } catch (Exception e) {
            log.error("마이페이지 로드 실패", e);
            return "redirect:/logout";
        }
    }


    //내 리뷰 목록
    @GetMapping("/reviews")
    public String myReviews(
            @RequestParam(defaultValue = "0") int page,
            HttpServletRequest request,
            Model model
    ) {
        Long myUserId = getUserIdFromToken(request);
        if (myUserId == null) {
            return "redirect:/login";
        }

        RestPage<BookReviewResponseDto> reviewPage = userClient.getUserReviews(myUserId, page, 10);

        model.addAttribute("reviews", reviewPage.getContent());
        model.addAttribute("page", reviewPage);

        model.addAttribute("isOwner", true);

        return "user/mypage/reviews";
    }

    //내 정보 수정 페이지
    @GetMapping("/edit")
    public String editForm(HttpServletRequest request, Model model) {
        String accessToken = CookieUtils.getCookieValue(request, "accessToken");
        if (accessToken == null) {
            return "redirect:/login";
        }

        try {
            UserResponseDto user = userClient.getMyInfo("Bearer " + accessToken);

            UserUpdateRequest updateRequest = new UserUpdateRequest(
                    user.getName(), user.getEmail(), user.getPhone(), user.getNickname()
            );
            model.addAttribute("userUpdateRequest", updateRequest);

            return "user/mypage/edit";
        } catch (Exception e) {
            return "redirect:/users/me";
        }
    }

    //내 정보 수정 처리
    @PostMapping("/edit")
    public String updateInfo(HttpServletRequest request,
                             @ModelAttribute UserUpdateRequest updateRequest,
                             Model model) {
        String accessToken = CookieUtils.getCookieValue(request, "accessToken");

        try {
            userClient.updateMyInfo("Bearer " + accessToken, updateRequest);

            return "redirect:/users/me?success=update";

        } catch (Exception e) {
            log.error("정보 수정 실패", e);
            model.addAttribute("error", "정보 수정에 실패했습니다. (" + e.getMessage() + ")");
            model.addAttribute("userUpdateRequest", updateRequest); // 입력값 유지
            return "user/mypage/edit";
        }
    }

    //주소 목록 페이지
    @GetMapping("/addresses")
    public String addressList(HttpServletRequest request, Model model) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");

        try {
            List<UserAddressResponseDto> addresses = userClient.getMyAddresses(token);
            model.addAttribute("addresses", addresses);
        } catch (Exception e) {
            model.addAttribute("addresses", List.of());
        }
        return "user/mypage/address-list";
    }

    //주소 추가 페이지
    @GetMapping("/addresses/me")
    public String createAddressForm(Model model) {
        model.addAttribute("addressForm", new UserAddressCreateRequest());
        return "user/mypage/address-form";
    }

    //배송지 저장
    @PostMapping("/addresses")
    public String createAddress(HttpServletRequest request,
                                @ModelAttribute UserAddressCreateRequest address) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");
        try {
            userClient.createAddress(token, address);
            return "redirect:/users/me/addresses";
        } catch (Exception e) {
            return "redirect:/users/me/addresses/new?error";
        }
    }

    //주소 수정 페이지
    @GetMapping("/addresses/{id}/edit")
    public String updateAddressForm(HttpServletRequest request, @PathVariable Long id, Model model) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");
        try {
            UserAddressResponseDto address = userClient.getAddressDetail(token, id);
            model.addAttribute("addressForm", address);
            model.addAttribute("isUpdate", true);
            return "user/mypage/address-form";
        } catch (Exception e) {
            return "redirect:/users/me/addresses";
        }
    }

    //주소 수정
    @PostMapping("/addresses/{id}/update")
    public String updateAddress(HttpServletRequest request,
                                @PathVariable Long id,
                                @ModelAttribute UserAddressUpdateRequest address) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");
        try {
            userClient.updateAddress(token, id, address);
            return "redirect:/users/me/addresses";
        } catch (Exception e) {
            return "redirect:/users/me/addresses/" + id + "/edit?error";
        }
    }

    //주소 삭제
    @PostMapping("/addresses/{id}/delete")
    public String deleteAddress(HttpServletRequest request, @PathVariable Long id) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");
        userClient.deleteAddress(token, id);
        return "redirect:/users/me/addresses";
    }

    //헬퍼 메서드
    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = CookieUtils.getCookieValue(request, "accessToken");
        if (token == null) {
            return null;
        }
        return JwtUtils.getUserId(token);
    }
}