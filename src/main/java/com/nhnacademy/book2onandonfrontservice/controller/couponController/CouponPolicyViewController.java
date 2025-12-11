package com.nhnacademy.book2onandonfrontservice.controller.couponController;

import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.client.CouponClient;
import com.nhnacademy.book2onandonfrontservice.client.CouponPolicyClient;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.CategoryDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponDto.CouponCreateDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.CouponPolicyDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.CouponPolicyUpdateDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyDiscountType;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyStatus;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyType;
import java.util.List;
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
@RequestMapping("/admin/policies")
@RequiredArgsConstructor
public class CouponPolicyViewController {

    private final CouponPolicyClient couponPolicyClient;
    private final CouponClient couponClient;
    private final BookClient bookClient;


    // 정책 목록 조회 페이지
    @GetMapping
    public String listPolicies(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int size,
                               @RequestParam(required = false) CouponPolicyType type,
                               @RequestParam(required = false) CouponPolicyDiscountType discountType,
                               @RequestParam(required = false) CouponPolicyStatus status,
                               Model model) {

        Page<CouponPolicyDto> policyPage = couponPolicyClient.getPolicies(page, size, type, discountType, status);

        model.addAttribute("policies", policyPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", policyPage.getTotalPages());

        model.addAttribute("searchType", type);
        model.addAttribute("searchDiscountType", discountType);
        model.addAttribute("searchStatus", status);

        // 페이지네이션 로직
        int startPage = Math.max(0, page - 2);
        int endPage = Math.min(policyPage.getTotalPages() - 1, page + 2);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "/admin/couponPolicy/list";
    }


    // 정책 등록 폼
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("policy", new CouponPolicyDto());
        List<CategoryDto> categories = bookClient.getCategories();
        model.addAttribute("categoryList", categories);
        model.addAttribute("pageTitle", "쿠폰 정책 등록");
        return "/admin/couponPolicy/form";
    }


    // 정책 등록 처리
    @PostMapping("/create")
    public String createPolicy(@ModelAttribute CouponPolicyDto requestDto) {
        couponPolicyClient.createPolicy(requestDto);
        return "redirect:/admin/policies";
    }


    // 정책 수정 폼
    @GetMapping("/update/{id}")
    public String updateForm(@PathVariable Long id, Model model) {
        CouponPolicyDto policy = couponPolicyClient.getPolicy(id);
        List<CategoryDto> categories = bookClient.getCategories();
        // UpdateDto로 변환
        boolean isMaxPriceNull = policy.getMaxPrice() == null;
        boolean isDurationNull = policy.getDurationDays() == null;
        boolean isDateNull = policy.getFixedStartDate() == null;
        boolean isBookNull = policy.getTargetBookIds() == null || policy.getTargetBookIds().isEmpty();
        boolean isCategoryNull = policy.getTargetCategoryIds() == null || policy.getTargetCategoryIds().isEmpty();
        CouponPolicyUpdateDto updateDto = new CouponPolicyUpdateDto(
                policy.getCouponPolicyId(),
                policy.getCouponPolicyName(),
                policy.getCouponPolicyType(),
                policy.getCouponPolicyDiscountType(),
                policy.getCouponDiscountValue(),
                policy.getMinPrice(),
                policy.getMaxPrice(),
                isMaxPriceNull,          // [수정] null이면 true
                policy.getDurationDays(),
                isDurationNull,          // [수정]
                policy.getFixedStartDate(),
                policy.getFixedEndDate(),
                isDateNull,              // [수정]
                policy.getTargetBookIds(),
                isBookNull,              // [수정]
                policy.getTargetCategoryIds(),
                isCategoryNull,          // [수정]
                policy.getCouponPolicyStatus()
        );

        model.addAttribute("categoryList", categories);
        model.addAttribute("policy", updateDto);
        model.addAttribute("pageTitle", "쿠폰 정책 수정");
        return "/admin/couponPolicy/form";
    }

    @PostMapping("/update/{id}")
    public String updatePolicy(@PathVariable Long id,
                               @ModelAttribute CouponPolicyUpdateDto requestDto) {

        // ModelAttribute를 직접 JSON 변환하여 보내기
        couponPolicyClient.updatePolicy(id, requestDto);

        return "redirect:/admin/policies";
    }

    // 정책 상세 페이지
    @GetMapping("/details/{id}")
    public String viewPolicyDetails(@PathVariable Long id, Model model) {
        CouponPolicyDto policy = couponPolicyClient.getPolicy(id); // 단일 정책 조회 API 호출

        model.addAttribute("policy", policy);
        model.addAttribute("pageTitle", "쿠폰 정책 상세 조회");

        return "/admin/couponPolicy/detail"; // detail 템플릿 반환
    }


    // 정책 비활성화 (삭제)
    @PostMapping("/delete/{id}")
    public String deactivatePolicy(@PathVariable Long id) {
        couponPolicyClient.deactivatePolicy(id);
        return "redirect:/admin/policies";
    }

    @PostMapping("/{id}/create-coupon")
    public String createCoupon(@PathVariable Long id,
                               @RequestParam(required = false) Integer quantity) {

        CouponCreateDto requestDto = new CouponCreateDto(quantity, id);
        couponClient.createCoupon(requestDto);
        return "redirect:/admin/policies/details/" + id;
    }

    // --- 공통 데이터 (드롭다운 메뉴용) ---

    @ModelAttribute("policyTypes")
    public CouponPolicyType[] policyTypes() {
        return CouponPolicyType.values();
    }

    @ModelAttribute("discountTypes")
    public CouponPolicyDiscountType[] discountTypes() {
        return CouponPolicyDiscountType.values();
    }

    @ModelAttribute("statuses")
    public CouponPolicyStatus[] statuses() {
        return CouponPolicyStatus.values();
    }
}
