package com.nhnacademy.book2onandonfrontservice.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("NotFoundBookException 발생 시 404 에러와 bookId 포함 메시지 반환 확인")
    void handleNotFoundBookException_Success() throws Exception {
        Long bookId = 100L;
        String expectedMessage = "bookId=" + bookId + "를 찾을 수 없습니다.";

        mockMvc.perform(get("/test/not-found-book/" + bookId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @RestController
    static class TestController {
        @GetMapping("/test/not-found-book/{bookId}")
        public void throwException(@org.springframework.web.bind.annotation.PathVariable Long bookId) {
            throw new NotFoundBookException(bookId);
        }
    }
}