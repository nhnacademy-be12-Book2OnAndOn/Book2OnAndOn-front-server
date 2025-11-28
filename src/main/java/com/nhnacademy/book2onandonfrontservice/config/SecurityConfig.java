package com.nhnacademy.book2onandonfrontservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/signup", "/css/**", "/js/**", "/images/**", "/books/**").permitAll()
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
 */