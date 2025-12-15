package com.nhnacademy.book2onandonfrontservice.controller.couponController;


import com.nhnacademy.book2onandonfrontservice.client.MemberCouponClient;
import com.nhnacademy.book2onandonfrontservice.dto.memberCouponDto.MemberCouponDto;
import com.nhnacademy.book2onandonfrontservice.dto.memberCouponDto.MemberCouponStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users/me/coupons")
public class MemberCouponViewController {

    private final MemberCouponClient memberCouponClient;

    @GetMapping
    public String myCouponList(@CookieValue(value = "accessToken", required = false) String accessToken,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) MemberCouponStatus status,
                               Model model) {
        if (accessToken == null) {
            return "redirect:/login";
        }

        Page<MemberCouponDto> myCouponPage = memberCouponClient.getMyCoupon("Bearer " + accessToken, page, size,
                status);

        model.addAttribute("myCoupons", myCouponPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", myCouponPage.getTotalPages());
        model.addAttribute("currentStatus", status);

        int startPage = Math.max(0, page - 2);
        int endPage = Math.min(myCouponPage.getTotalPages() - 1, page + 2);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "memberCoupon";
    }

}
