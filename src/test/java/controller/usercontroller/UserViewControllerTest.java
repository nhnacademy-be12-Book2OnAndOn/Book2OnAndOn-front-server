package controller.usercontroller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.controller.userController.UserViewController;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.RestPage;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.BookReviewResponseDto;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

class UserViewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private UserViewController userViewController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(userViewController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    @DisplayName("특정 사용자의 리뷰 목록 조회 성공")
    void otherReviews_Success() throws Exception {
        RestPage<BookReviewResponseDto> mockPage = new RestPage<>(
                List.of(),
                PageRequest.of(0, 10),
                0L
        );

        given(userClient.getUserReviews(anyLong(), anyInt(), anyInt()))
                .willReturn(mockPage);

        mockMvc.perform(get("/users/123/reviews")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/reviews"))
                .andExpect(model().attributeExists("reviews", "page", "targetUserId"))
                .andExpect(model().attribute("targetUserId", 123L));
    }
}
