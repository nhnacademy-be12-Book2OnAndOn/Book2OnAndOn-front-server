package com.nhnacademy.book2onandonfrontservice.controller.couponController;

import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.client.CouponClient;
import com.nhnacademy.book2onandonfrontservice.client.CouponPolicyClient;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDetailResponse;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDto;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookSearchCondition;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.CategoryDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponDto.CouponCreateDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.CouponPolicyDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.CouponPolicyUpdateDto;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyDiscountType;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyStatus;
import com.nhnacademy.book2onandonfrontservice.dto.couponPolicyDto.enums.CouponPolicyType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
                               @RequestParam(defaultValue = "10") int size,
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

        int startPage = Math.max(0, page - 2);
        int endPage = Math.min(policyPage.getTotalPages() - 1, page + 2);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "/admin/couponPolicy/list";
    }

    // --- 2. 정책 등록 폼 ---
    @GetMapping("/create")
    public String createForm(Model model) {
        // DTO의 List 필드는 내부적으로 new ArrayList<>() 초기화 되어 있어야 함
        model.addAttribute("policy", new CouponPolicyUpdateDto());

        List<CategoryDto> categories = bookClient.getCategories();
        model.addAttribute("categoryList", categories);

        model.addAttribute("pageTitle", "쿠폰 정책 등록");
        return "/admin/couponPolicy/form";
    }

    // --- 3. 정책 등록 처리 ---
    @PostMapping("/create")
    public String createPolicy(@ModelAttribute CouponPolicyUpdateDto requestDto) {
        // 별도 변환 없이 바로 넘김 (Spring이 List<Long> 바인딩 처리)
        couponPolicyClient.createPolicy(requestDto);
        return "redirect:/admin/policies";
    }

    // --- 4. 정책 수정 폼 (핵심: 이름 매핑 정보 추가) ---
    @GetMapping("/update/{id}")
    public String updateForm(@PathVariable Long id, Model model) {
        CouponPolicyDto policy = couponPolicyClient.getPolicy(id);
        List<CategoryDto> categories = bookClient.getCategories();

        // [DTO 변환] Null 처리 로직
        boolean isMaxPriceNull = policy.getMaxPrice() == null;
        boolean isDurationNull = policy.getDurationDays() == null;
        boolean isDateNull = policy.getFixedStartDate() == null;
        boolean isBookNull = policy.getTargetBookIds() == null || policy.getTargetBookIds().isEmpty();
        boolean isCategoryNull = policy.getTargetCategoryIds() == null || policy.getTargetCategoryIds().isEmpty();

        // 1. DTO 생성 (List<Long> 그대로 사용)
        CouponPolicyUpdateDto updateDto = new CouponPolicyUpdateDto(
                policy.getCouponPolicyId(),
                policy.getCouponPolicyName(),
                policy.getCouponPolicyType(),
                policy.getCouponPolicyDiscountType(),
                policy.getCouponDiscountValue(),
                policy.getMinPrice(),
                policy.getMaxPrice(),
                isMaxPriceNull,
                policy.getDurationDays(),
                isDurationNull,
                policy.getFixedStartDate(),
                policy.getFixedEndDate(),
                isDateNull,
                policy.getTargetBookIds(),     // List<Long>
                isBookNull,
                policy.getTargetCategoryIds(), // List<Long>
                isCategoryNull,
                policy.getCouponPolicyStatus()
        );

        // 도서 ID -> 도서 제목(Title) 매핑 Map 생성
        Map<Long, String> bookNameMap = new HashMap<>();
        if (!isBookNull) {
            for (Long bookId : policy.getTargetBookIds()) {
                try {
                    BookDetailResponse book = bookClient.getBookDetail(bookId);
                    bookNameMap.put(bookId, book.getTitle());
                } catch (Exception e) {
                    bookNameMap.put(bookId, "정보 없음(ID:" + bookId + ")");
                }
            }
        }

        // 카테고리 ID -> 카테고리 이름(Name) 매핑 Map 생성
        Map<Long, String> categoryNameMap = new HashMap<>();
        if (!isCategoryNull) {
            // 전체 카테고리 트리에서 현재 선택된 ID들의 이름을 찾아 Map에 담음
            flattenCategoryTree(categories, categoryNameMap, policy.getTargetCategoryIds());
        }

        model.addAttribute("categoryList", categories);
        model.addAttribute("policy", updateDto);
        model.addAttribute("bookNameMap", bookNameMap);         // HTML에서 이름 표시용
        model.addAttribute("categoryNameMap", categoryNameMap); // HTML에서 이름 표시용
        model.addAttribute("pageTitle", "쿠폰 정책 수정");

        return "/admin/couponPolicy/form";
    }

    // 정책 수정 처리
    @PostMapping("/update/{id}")
    public String updatePolicy(@PathVariable Long id,
                               @ModelAttribute CouponPolicyUpdateDto requestDto) {
        couponPolicyClient.updatePolicy(id, requestDto);
        return "redirect:/admin/policies";
    }

    // 정책 상세 페이지
    @GetMapping("/details/{id}")
    public String viewPolicyDetails(@PathVariable Long id, Model model) {
        CouponPolicyDto policy = couponPolicyClient.getPolicy(id);

        // 도서 이름 매핑 (ID -> Title)
        Map<Long, String> bookNameMap = new HashMap<>();
        if (policy.getTargetBookIds() != null && !policy.getTargetBookIds().isEmpty()) {
            for (Long bookId : policy.getTargetBookIds()) {
                try {
                    // BookClient 상세 조회 사용
                    BookDetailResponse book = bookClient.getBookDetail(bookId);
                    bookNameMap.put(bookId, book.getTitle());
                } catch (Exception e) {
                    bookNameMap.put(bookId, "정보 없음(ID:" + bookId + ")");
                }
            }
        }

        // 카테고리 이름 매핑 (ID -> Name)
        Map<Long, String> categoryNameMap = new HashMap<>();
        if (policy.getTargetCategoryIds() != null && !policy.getTargetCategoryIds().isEmpty()) {
            List<CategoryDto> categories = bookClient.getCategories(); // 전체 트리 가져오기
            flattenCategoryTree(categories, categoryNameMap, policy.getTargetCategoryIds());
        }

        model.addAttribute("policy", policy);
        model.addAttribute("bookNameMap", bookNameMap);         // [NEW]
        model.addAttribute("categoryNameMap", categoryNameMap); // [NEW]
        model.addAttribute("pageTitle", "쿠폰 정책 상세 조회");

        return "/admin/couponPolicy/detail";
    }

    @PostMapping("/delete/{id}")
    public String deactivatePolicy(@PathVariable Long id) {
        couponPolicyClient.deactivatePolicy(id);
        return "redirect:/admin/policies";
    }

    @PostMapping("/{id}/create-coupon")
    public String createCoupon(@PathVariable Long id, @RequestParam(required = false) Integer quantity) {
        CouponCreateDto requestDto = new CouponCreateDto(quantity, id);
        couponClient.createCoupon(requestDto);
        return "redirect:/admin/policies/details/" + id;
    }

    @GetMapping("/books/search")
    @ResponseBody
    public ResponseEntity<Page<BookDto>> searchBooksForPolicy(@RequestParam("keyword") String keyword,
                                                              @PageableDefault(size = 5) Pageable pageable) {
        BookSearchCondition condition = BookSearchCondition.builder()
                .keyword(keyword)
                .build();
        Page<BookDto> result = bookClient.searchBooks(condition, pageable);
        return ResponseEntity.ok(result);
    }

    // 카테고리 트리에서 이름 찾기 (재귀)
    private void flattenCategoryTree(List<CategoryDto> nodes, Map<Long, String> map, List<Long> targetIds) {
        if (nodes == null) return;
        for (CategoryDto node : nodes) {
            // 찾는 대상(targetIds)에 현재 노드가 포함되어 있다면 Map에 저장
            if (targetIds.contains(node.getId())) {
                map.put(node.getId(), node.getName());
            }
            // 자식 노드 재귀 탐색
            flattenCategoryTree(node.getChildren(), map, targetIds);
        }
    }
    
    @ModelAttribute("policyTypes")
    public CouponPolicyType[] policyTypes() { return CouponPolicyType.values(); }

    @ModelAttribute("discountTypes")
    public CouponPolicyDiscountType[] discountTypes() { return CouponPolicyDiscountType.values(); }

    @ModelAttribute("statuses")
    public CouponPolicyStatus[] statuses() { return CouponPolicyStatus.values(); }
}