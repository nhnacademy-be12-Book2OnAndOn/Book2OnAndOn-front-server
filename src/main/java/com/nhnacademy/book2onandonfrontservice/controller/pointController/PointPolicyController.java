package com.nhnacademy.book2onandonfrontservice.controller.pointController;

import com.nhnacademy.book2onandonfrontservice.client.PointPolicyAdminClient;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointPolicy.PointPolicyActiveUpdateRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointPolicy.PointPolicyResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointPolicy.PointPolicyUpdateRequestDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/point-policies")
@RequiredArgsConstructor
public class PointPolicyController {

    private final PointPolicyAdminClient pointPolicyAdminClient;

    @GetMapping
    public String listPolicies(@CookieValue(value = "accessToken", required = false) String accessToken, Model model) {
        List<PointPolicyResponseDto> policies = pointPolicyAdminClient.getAllPolicies("Bearer " + accessToken);

        model.addAttribute("policies", policies);
        return "admin/point-policy"; // 리스트 템플릿
    }

    @GetMapping("/{policyName}")
    public String viewPolicy(@PathVariable String policyName,
                             @CookieValue(value = "accessToken", required = false) String accessToken,
                             Model model) {
        PointPolicyResponseDto policy = pointPolicyAdminClient.getPolicy(policyName, "Bearer " + accessToken);

        model.addAttribute("policy", policy);
        return "admin/point-policy"; // 상세 템플릿
    }

    @PostMapping("/{policyId}")
    public String updatePolicy(@PathVariable Integer policyId,
                               @ModelAttribute PointPolicyUpdateRequestDto requestDto,
                               @CookieValue(value = "accessToken", required = false) String accessToken) {

        pointPolicyAdminClient.updatePolicy(policyId, requestDto, "Bearer " + accessToken);

        // 수정 후 해당 정책 상세 페이지로 이동
        return "redirect:/admin/point-policies/" + policyId;
    }

    @PostMapping("/{policyId}/active")
    public String updatePolicyActive(@PathVariable Integer policyId,
                                     @ModelAttribute PointPolicyActiveUpdateRequestDto requestDto,
                                     @CookieValue(value = "accessToken", required = false) String accessToken) {

        pointPolicyAdminClient.updatePolicyActive(policyId, requestDto, "Bearer " + accessToken);

        return "redirect:/admin/point-policies/" + policyId;
    }
}
