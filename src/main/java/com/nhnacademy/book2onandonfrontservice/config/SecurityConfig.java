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
                .headers(headers -> headers
                        .xssProtection(xss -> xss
                                .headerValue(HeaderValue.ENABLED_MODE_BLOCK)
                        )
                        // [수정된 부분] Cloudflare 도메인 추가
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "script-src 'self' 'unsafe-inline' https://t1.daumcdn.net https://uicdn.toast.com https://static.cloudflareinsights.com; "
                                        + "style-src 'self' 'unsafe-inline' https://uicdn.toast.com; "
                                        + "object-src 'none'; base-uri 'self';"))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/webjars/**", "/login", "/signup", "/css/**", "/js/**", "/images/**",
                                "/books/**")
                        .permitAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}