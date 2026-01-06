package com.nhnacademy.book2onandonfrontservice;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class FeignExceptionAdviceTest {

    private MockMvc mockMvc;
    private static FeignException mockException;

    @BeforeEach
    void setUp() {
        mockException = mock(FeignException.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new FeignExceptionAdvice())
                .build();
    }

    @Test
    @DisplayName("FeignException 발생 시 상태 코드와 바디 반환 확인")
    void handleFeign_StandardStatus() throws Exception {
        when(mockException.status()).thenReturn(400);
        when(mockException.contentUTF8()).thenReturn("Bad Request from Server");

        mockMvc.perform(get("/test-feign"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Bad Request from Server"));
    }

    @Test
    @DisplayName("상태 코드가 0 이하일 경우 502 코드로 변환 확인")
    void handleFeign_InvalidStatus_Returns502() throws Exception {
        when(mockException.status()).thenReturn(-1);
        when(mockException.contentUTF8()).thenReturn("Gateway Error");

        mockMvc.perform(get("/test-feign"))
                .andExpect(status().isBadGateway())
                .andExpect(content().string("Gateway Error"));
    }

    @RestController
    static class TestController {
        @GetMapping("/test-feign")
        public void throwException() {
            throw mockException;
        }
    }
}
