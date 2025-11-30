package com.nhnacademy.book2onandonfrontservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                //X-XSS-Protection 헤더 설정
                //브라우저의 내장 XSS필터를 활성화하고, 공격 감지시 페이지 로드 차단
                .headers(headers -> headers
                        .xssProtection(xss -> xss
                                .headerValue(HeaderValue.ENABLED_MODE_BLOCK)
                        )
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "script-src 'self' 'unsafe-inLine'; object-src 'none'; base-uri 'self';"))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/signup", "/css/**", "/js/**", "/images/**", "/books/**")
                        .permitAll()
                        .anyRequest().permitAll() // 프론트는 인증 기능 자체가 없음
                );

        return http.build();
    }
}

/*
             프론트 서버는 로그인 인증을 자체 처리하지 않고,
             Gateway -> UserService로 REST 방식 인증을 전달

             Spring Security의 CSRF 보호는 서버 템플릿 기반 로그인에서만 필요하지만,
             우리는 외부 API로 인증 요청을 보내므로 CSRF가 요청을 막아버림.

             실제 문제:
             - 로그인 폼에 CSRF 토큰이 자동으로 붙고
             - Spring Security가 POST /login 요청을 차단 -> 로그인 실패 반복

             해결:
             - CSRF를 disable 해야 AuthViewController가 동작
             XSS헤더: 사용자가 악성 스크립트가 포함된 URL을 클릭했을 때, 브라우저가 이를 감지하면 페이지 렌더링을 멈춰버리게함.
             CSP(ContentSecurityPolicy): 우리 서버에서 제공한 자바스크립트 파일만 실행하라고 브라우저에게 명령함. 외부 사이트의 악성 스크립트를 심어도 브라우저가 거절.
 */