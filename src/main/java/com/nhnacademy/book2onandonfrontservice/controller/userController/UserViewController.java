package com.nhnacademy.book2onandonfrontservice.controller.userController;

import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.RestPage;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.BookReviewResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserViewController {
    private final UserClient userClient;

    @GetMapping("/{userId}/reviews")
    public String otherReviews(@PathVariable Long userId, Model model,
                               @RequestParam(defaultValue = "0") int page) {
        RestPage<BookReviewResponseDto> reviews = userClient.getUserReviews(userId, page, 10);

        model.addAttribute("reviews", reviews.getContent());
        model.addAttribute("page", reviews);
        model.addAttribute("targetUserId", userId);

        return "user/reviews";
    }
}
