package com.nhnacademy.book2onandonfrontservice.controller.userController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.client.MemberCouponClient;
import com.nhnacademy.book2onandonfrontservice.client.PointUserClient;
import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.client.UserGradeClient;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.MyLikedBookResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.memberCouponDto.MemberCouponDto;
import com.nhnacademy.book2onandonfrontservice.dto.memberCouponDto.MemberCouponStatus;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.CurrentPointResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory.PointHistoryResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.RestPage;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.UserGradeDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.PasswordChangeRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.UserAddressCreateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.UserAddressUpdateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.UserUpdateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.BookReviewResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.UserAddressResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.UserResponseDto;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import com.nhnacademy.book2onandonfrontservice.util.JwtUtils;
import feign.FeignException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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


//마이페이지 (accessToken 접근 테스트용 임시)
@Slf4j
@Controller
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class MyPageViewController {

    private final UserClient userClient;
    private final BookClient bookClient;
    private final MemberCouponClient memberCouponClient;
    private final PointUserClient pointUserClient;
    private final UserGradeClient userGradeClient;

    //마이페이지
    @GetMapping
    public String myPageHome(HttpServletRequest request, Model model) {
        String accessToken = CookieUtils.getCookieValue(request, "accessToken");
        Long myUserId = JwtUtils.getUserId(accessToken);

        if (myUserId == null) {
            return "redirect:/login";
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

            try {
                Page<MemberCouponDto> memberCouponPage = memberCouponClient.getMyCoupon("Bearer " + accessToken, 0, 1,
                        MemberCouponStatus.NOT_USED);

                log.info("쿠폰 갯수: {}", memberCouponPage.getTotalElements());
                model.addAttribute("couponCount", memberCouponPage.getTotalElements());
                model.addAttribute("recentCoupons", toRecentCoupons(memberCouponPage.getContent()));
            } catch (Exception e) {
                log.warn("쿠폰 개수 조회 실패", e);
                model.addAttribute("couponCount", 0);
                model.addAttribute("recentCoupons", List.of());
            }

            model.addAttribute("orderCount", 0);
            model.addAttribute("recentOrders", List.of());
            model.addAttribute("defaultAddress", resolveDefaultAddress(accessToken));

            try {
                Page<PointHistoryResponseDto> pointHistoryPage =
                        pointUserClient.getMyPointHistory("Bearer " + accessToken, 0, 1);
                model.addAttribute("recentPointHistory", toRecentPoints(pointHistoryPage.getContent()));
            } catch (Exception e) {
                log.warn("포인트 내역 조회 실패", e);
                model.addAttribute("recentPointHistory", List.of());
            }

            try {
                CurrentPointResponseDto pointDto = pointUserClient.getMyCurrentPoint("Bearer " + accessToken);
                model.addAttribute("currentPoint", pointDto.getCurrentPoint());
            } catch (Exception e) {
                log.warn("포인트 조회 실패", e);
                model.addAttribute("currentPoint", 0);
            }

            return "user/mypage/index";

        } catch (Exception e) {
            log.error("마이페이지 로드 실패", e);
            return "redirect:/logout";
        }
    }

    private List<RecentCouponView> toRecentCoupons(List<MemberCouponDto> coupons) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<RecentCouponView> views = new ArrayList<>();

        coupons.stream()
                .sorted(Comparator.comparing(MemberCouponDto::getMemberCouponEndDate))
                .limit(1)
                .forEach(coupon -> views.add(new RecentCouponView(
                        coupon.getCouponName(),
                        getCouponStatusLabel(coupon.getMemberCouponStatus()),
                        coupon.getMemberCouponEndDate() != null
                                ? coupon.getMemberCouponEndDate().format(formatter)
                                : "-",
                        coupon.getDiscountDescription() != null ? coupon.getDiscountDescription() : ""
                )));

        return views;
    }

    private List<RecentPointView> toRecentPoints(List<PointHistoryResponseDto> points) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.KOREA);
        List<RecentPointView> views = new ArrayList<>();

        points.stream()
                .sorted(Comparator.comparing(PointHistoryResponseDto::getPointCreatedDate).reversed())
                .limit(1)
                .forEach(point -> {
                    String amount = (point.getPointHistoryChange() >= 0 ? "+" : "")
                            + numberFormat.format(point.getPointHistoryChange()) + " P";
                    String balance = numberFormat.format(point.getTotalPoints()) + " P";

                    views.add(new RecentPointView(
                            getReasonLabel(point.getPointReason()),
                            point.getPointCreatedDate() != null
                                    ? point.getPointCreatedDate().format(formatter)
                                    : "-",
                            amount,
                            balance
                    ));
                });

        return views;
    }

    private String resolveDefaultAddress(String accessToken) {
        try {
            return userClient.getMyAddresses("Bearer " + accessToken).stream()
                    .filter(UserAddressResponseDto::isDefault)
                    .findFirst()
                    .map(addr -> {
                        String detail = addr.getUserAddressDetail() != null ? " " + addr.getUserAddressDetail() : "";
                        return addr.getUserAddress() + detail;
                    })
                    .orElse(null);
        } catch (Exception e) {
            log.warn("기본 주소 조회 실패", e);
            return null;
        }
    }

    private String getReasonLabel(com.nhnacademy.book2onandonfrontservice.dto.pointDto.PointReason reason) {
        if (reason == null) {
            return "-";
        }
        return switch (reason) {
            case SIGNUP -> "회원가입 적립";
            case REVIEW -> "리뷰 적립";
            case ORDER -> "주문 적립";
            case USE -> "포인트 사용";
            case REFUND -> "포인트 반환";
            case EXPIRE -> "포인트 만료";
            case ADMIN_ADJUST -> "관리자 조정";
        };
    }

    private String getCouponStatusLabel(MemberCouponStatus status) {
        if (status == null) {
            return "-";
        }
        return switch (status) {
            case NOT_USED -> "사용 가능";
            case USED -> "사용 완료";
            case EXPIRED -> "만료됨";
        };
    }

    private record RecentCouponView(String name, String status, String expireDate, String detail) {
    }

    private record RecentPointView(String reason, String date, String amount, String balance) {
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

    // 내 좋아요 목록
    @GetMapping("/likes")
    public String myLikes(@RequestParam(defaultValue = "0") int page,
                          HttpServletRequest request,
                          Model model) {

        String accessToken = CookieUtils.getCookieValue(request, "accessToken");
        if (accessToken == null) {
            return "redirect:/login";
        }

        try {
            UserResponseDto myInfo = userClient.getMyInfo("Bearer " + accessToken);
            model.addAttribute("user", myInfo);

            RestPage<MyLikedBookResponseDto> rest = userClient.getMyLikedBooks("Bearer " + accessToken, page, 12);

            model.addAttribute("books", rest.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", rest.getTotalPages());

        } catch (Exception e) {
            log.error("좋아요 목록 조회 중 오류 발생 (Book Service 문제 가능성): {}", e.getMessage());

            model.addAttribute("books", List.of());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("error", "시스템 오류로 인해 좋아요 목록을 불러올 수 없습니다.");
        }

        return "user/mypage/likes";
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

            log.info("내 정보 조회 결과: name={}, email={}, phone={}", user.getName(), user.getEmail(), user.getPhone());

            UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                    .name(user.getName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .nickname(user.getNickname())
                    .build();

            model.addAttribute("userUpdateRequest", updateRequest);
            model.addAttribute("gradeName", user.getGradeName());

            try {
                List<UserGradeDto> allGrades = userGradeClient.getAllGrades();
                allGrades.sort(Comparator.comparingInt(UserGradeDto::getPointCutline));
                model.addAttribute("allGrades", allGrades);
            } catch (Exception e) {
                log.warn("등급 목록 조회 실패", e);
                model.addAttribute("allGrades", List.of());
            }

            return "user/mypage/edit";
        } catch (Exception e) {
            log.error("내 정보 조회 실패", e);
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

        } catch (FeignException e) {
            log.error("정보 수정 API 호출 에러", e);

            String errorMessage = "정보 수정에 실패했습니다.";

            try {
                String responseBody = e.contentUTF8();

                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> errorMap = objectMapper.readValue(responseBody, Map.class);

                if (errorMap != null && errorMap.containsKey("message")) {
                    errorMessage += " (" + errorMap.get("message") + ")";
                }
            } catch (Exception parsingEx) {
                log.error("에러 메시지 파싱 실패", parsingEx);
            }

            model.addAttribute("error", errorMessage);
            model.addAttribute("userUpdateRequest", updateRequest);
            return "user/mypage/edit";

        } catch (Exception e) {
            log.error("정보 수정 중 알 수 없는 오류", e);
            model.addAttribute("error", "정보 수정에 실패했습니다. (시스템 오류)");
            model.addAttribute("userUpdateRequest", updateRequest);
            return "user/mypage/edit";
        }
    }

    //비밀번호 변경 페이지
    @GetMapping("/password")
    public String passwordForm(HttpServletRequest request, Model model) {
        if (CookieUtils.getCookieValue(request, "accessToken") == null) {
            return "redirect:/login";
        }
        model.addAttribute("passwordRequest", new PasswordChangeRequest());
        return "user/mypage/password-change";
    }

    //비밀번호 변경
    @PostMapping("/password")
    public String changePassword(HttpServletRequest request,
                                 @ModelAttribute PasswordChangeRequest passwordRequest,
                                 Model model) {
        String accessToken = CookieUtils.getCookieValue(request, "accessToken");

        if (passwordRequest.getCurrentPassword().equals(passwordRequest.getNewPassword())) {
            model.addAttribute("error", "새 비밀번호는 현재 비밀번호와 다르게 설정해야 합니다.");
            return "user/mypage/password-change";
        }

        if (!passwordRequest.getNewPassword().equals(passwordRequest.getNewPasswordConfirm())) {
            model.addAttribute("error", "새 비밀번호가 일치하지 않습니다.");
            return "user/mypage/password-change";
        }

        try {
            userClient.changePassword("Bearer " + accessToken, passwordRequest);

            // 3. 성공 시 로그아웃 시키거나(보안 강화), 대시보드로 이동
            return "redirect:/users/me?success=pw_changed";

        } catch (Exception e) {
            log.error("비밀번호 변경 실패", e);
            model.addAttribute("error", "현재 비밀번호가 일치하지 않거나 변경에 실패했습니다.");
            return "user/mypage/password-change";
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
    @GetMapping("/addresses/new")
    public String createAddressForm(Model model) {
        model.addAttribute("addressForm", new UserAddressCreateRequest());
        model.addAttribute("isUpdate", false);
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

            UserAddressUpdateRequest form = new UserAddressUpdateRequest(
                    address.getUserAddressName(),
                    address.getRecipient(),
                    address.getPhone(),
                    address.getZipCode(),
                    address.getUserAddress(),
                    address.getUserAddressDetail(),
                    address.isDefault()
            );

            model.addAttribute("addressForm", form);
            model.addAttribute("addressId", id);
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

    // 회원 탈퇴페이지
    @GetMapping("/withdraw")
    public String withdrawPage(Model model) {
        return "user/mypage/withdraw";
    }

    // 회원 탈퇴 요청
    @PostMapping("/withdraw")
    public String withdraw(HttpServletRequest request,
                           HttpServletResponse response,
                           @RequestParam(required = false) String reason) {

        String accessToken = CookieUtils.getCookieValue(request, "accessToken");
        if (accessToken == null) {
            return "redirect:/login";
        }

        try {

            String finalReason = (reason == null || reason.isBlank()) ? "사용자 요청에 의한 탈퇴" : reason;
            userClient.withdrawUser("Bearer " + accessToken, finalReason);

            expireCookie(response, "accessToken");
            expireCookie(response, "refreshToken");

            return "redirect:/?message=withdrawn";

        } catch (Exception e) {
            log.error("회원 탈퇴 실패", e);
            return "redirect:/users/me?error=withdraw_failed";
        }
    }

    //헬퍼 메서드
    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = CookieUtils.getCookieValue(request, "accessToken");
        if (token == null) {
            return null;
        }
        return JwtUtils.getUserId(token);
    }

    private void expireCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
