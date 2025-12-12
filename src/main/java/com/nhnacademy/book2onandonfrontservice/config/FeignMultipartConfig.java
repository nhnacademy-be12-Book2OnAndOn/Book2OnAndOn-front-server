package com.nhnacademy.book2onandonfrontservice.config;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class FeignMultipartConfig {

    @Bean
    public Encoder multipartFormEncoder() {
        // SpringFormEncoder를 써야 파일(Multipart)과 객체(JSON)를 같이 보낼 수 있음
        return new SpringFormEncoder(
                new SpringEncoder(() -> new HttpMessageConverters(new RestTemplate().getMessageConverters())));
    }
}