package com.nhnacademy.book2onandonfrontservice.controller.pointController;

import com.nhnacademy.book2onandonfrontservice.client.PointPolicyAdminClient;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointPolicy.PointPolicyActiveUpdateRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointPolicy.PointPolicyResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointPolicy.PointPolicyUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/point-policies")
@RequiredArgsConstructor
public class PointPolicyController {

    private final PointPolicyAdminClient pointPolicyAdminClient;

    @GetMapping
    public String listPolicies(@RequestHeader("X-USER-ID") Long userId, Model model) {
        List<PointPolicyResponseDto> policies = pointPolicyAdminClient.getAllPolicies(userId);

        model.addAttribute("policies", policies);
        return "admin/point-policy"; // 리스트 템플릿
    }

    @GetMapping("/{policyName}")
    public String viewPolicy(@PathVariable String policyName,
                             @RequestHeader("X-USER-ID") Long userId,
                             Model model) {
        PointPolicyResponseDto policy = pointPolicyAdminClient.getPolicy(policyName, userId);

        model.addAttribute("policy", policy);
        return "admin/point-policy"; // 상세 템플릿
    }

    @PostMapping("/{policyId}")
    public String updatePolicy(@PathVariable Integer policyId,
                               @ModelAttribute PointPolicyUpdateRequestDto requestDto,
                               @RequestHeader("X-USER-ID") Long userId) {

        pointPolicyAdminClient.updatePolicy(policyId, requestDto, userId);

        // 수정 후 해당 정책 상세 페이지로 이동
        return "redirect:/admin/point-policies/" + policyId;
    }

    @PostMapping("/{policyId}/active")
    public String updatePolicyActive(@PathVariable Integer policyId,
                                     @ModelAttribute PointPolicyActiveUpdateRequestDto requestDto,
                                     @RequestHeader("X-USER-ID") Long userId) {

        pointPolicyAdminClient.updatePolicyActive(policyId, requestDto, userId);

        return "redirect:/admin/point-policies/" + policyId;
    }
}
