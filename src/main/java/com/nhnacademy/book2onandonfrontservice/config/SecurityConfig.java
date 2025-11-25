package com.nhnacademy.book2onandonfrontservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/**", "/login", "/signup", "/css/**", "/js/**", "/images/**").permitAll() // 정적 자원 및 로그인 허용
                .anyRequest().authenticated() // 나머지는 로그인 필요
            )
            .formLogin(form -> form
                .loginPage("/login") // 우리가 만든 login.html 사용
                .loginProcessingUrl("/login") // form action="@{/login}"과 일치
                .defaultSuccessUrl("/") // 성공 시 홈으로
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
            );
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}