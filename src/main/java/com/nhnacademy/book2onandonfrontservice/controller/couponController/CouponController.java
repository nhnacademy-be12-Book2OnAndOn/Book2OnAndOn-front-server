package com.nhnacademy.book2onandonfrontservice.controller.couponController;

import com.nhnacademy.book2onandonfrontservice.client.CouponClient;
import com.nhnacademy.book2onandonfrontservice.dto.couponDto.CouponDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponDto.CouponUpdateDto;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponClient couponClient;

    @GetMapping
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

    @PostMapping("{couponId}/update-quantity")
    public String updateQuantity(@PathVariable("couponId") Long couponId,
                                 @RequestParam(required = false) Integer quantity) {

        CouponUpdateDto updateDto = new CouponUpdateDto(quantity);
        couponClient.updateCouponQuantity(couponId, updateDto);

        return "redirect:/admin/coupons";
    }
}
