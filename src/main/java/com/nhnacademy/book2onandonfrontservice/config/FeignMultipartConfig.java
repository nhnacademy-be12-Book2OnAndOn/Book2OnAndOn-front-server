package com.nhnacademy.book2onandonfrontservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FeignMultipartConfig {

    private final ObjectFactory<HttpMessageConverters> messageConverters;
    private final ObjectMapper objectMapper;

    @Bean
    public Encoder multipartFormEncoder() {
        // 1. 기본 SpringEncoder 생성
        Encoder springEncoder = new SpringEncoder(messageConverters);
        // 2. 멀티파트용 SpringFormEncoder로 감싸기
        SpringFormEncoder formEncoder = new SpringFormEncoder(springEncoder);
        // 3. 우리가 만든 JSON 변환 Encoder로 한 번 더 감싸기 (Decorator 패턴)
        return new JsonMultipartEncoder(formEncoder, objectMapper);
    }
}