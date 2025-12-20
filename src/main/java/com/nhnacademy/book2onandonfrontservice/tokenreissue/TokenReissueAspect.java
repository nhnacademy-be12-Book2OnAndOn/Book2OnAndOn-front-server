package com.nhnacademy.book2onandonfrontservice.tokenreissue;

import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.ReissueRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.TokenResponseDto;
import com.nhnacademy.book2onandonfrontservice.service.FrontTokenService;
import com.nhnacademy.book2onandonfrontservice.util.JwtUtils;
import feign.FeignException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenReissueAspect {

    private final UserClient userClient;
    private final FrontTokenService tokenService;

    // 진행 중인 재발급 요청 관리 (동시성 제어용)
    private static final ConcurrentHashMap<String, CompletableFuture<TokenResponseDto>> pendingReissues = new ConcurrentHashMap<>();

    // 재발급 완료된 결과 잠시 저장 (Refresh Token Rotation 문제 해결용)
    private static final Map<String, TokenResponseDto> rotatedTokenCache = new ConcurrentHashMap<>();

    @Around("execution(* com.nhnacademy.book2onandonfrontservice.client..*(..))")
    public Object handle401(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (FeignException.Unauthorized e) {
            String methodName = joinPoint.getSignature().getName();
            if (methodName.equals("login") || methodName.equals("reissue")) {
                throw e;
            }

            log.info("Access Token 만료 감지(401). 재발급 로직 진입");

            String refreshToken = tokenService.getRefreshToken();
            String accessToken = tokenService.getAccessToken();

            if (refreshToken == null) {
                log.error("Refresh Token이 없어 재발급 불가");
                throw e;
            }

            if (rotatedTokenCache.containsKey(refreshToken)) { //RTR 방식으로 AccessToken 재발급시 RefreshToken 교체
                log.info("이미 교체된 Refresh Token입니다. 캐시된 토큰을 사용합니다.");
                return retryWithCachedToken(joinPoint, rotatedTokenCache.get(refreshToken));
            }

            TokenResponseDto newToken;

            // 동시성 제어
            CompletableFuture<TokenResponseDto> future = pendingReissues.get(refreshToken);

            if (future == null) {
                CompletableFuture<TokenResponseDto> newFuture = new CompletableFuture<>();
                future = pendingReissues.putIfAbsent(refreshToken, newFuture);

                if (future == null) {
                    future = newFuture;
                    try {
                        if (rotatedTokenCache.containsKey(refreshToken)) {
                            TokenResponseDto cached = rotatedTokenCache.get(refreshToken);
                            future.complete(cached);
                        } else {
                            log.info("대표 스레드가 재발급 API를 호출합니다.");
                            TokenResponseDto result = userClient.reissue(
                                    new ReissueRequestDto(accessToken, refreshToken));

                            rotatedTokenCache.put(refreshToken, result);
                            cleanupCacheAsync(refreshToken);

                            future.complete(result);
                        }
                    } catch (Exception ex) {
                        future.completeExceptionally(ex);
                        pendingReissues.remove(refreshToken);
                        throw ex;
                    } finally {
                        pendingReissues.remove(refreshToken);
                    }
                }
            }

            try {
                newToken = future.join();
            } catch (Exception ex) {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                log.error("토큰 재발급 실패: {}", cause.getMessage());
                throw cause instanceof FeignException ? (FeignException) cause : e;
            }

            // 결과 적용 및 재요청
            return retryWithCachedToken(joinPoint, newToken);
        }
    }

    // 결과 적용 및 재요청 공통 메서드
    private Object retryWithCachedToken(ProceedingJoinPoint joinPoint, TokenResponseDto tokenDto) throws Throwable {
        // 쿠키 갱신
        tokenService.updateTokens(tokenDto);

        // SecurityContext 동기화
        syncSecurityContext(tokenDto.getAccessToken());

        // 인자 교체 후 재요청
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String && ((String) args[i]).startsWith("Bearer ")) {
                args[i] = "Bearer " + tokenDto.getAccessToken();
            }
        }

        log.info("토큰 갱신 및 컨텍스트 설정 완료. 재요청 진행");
        return joinPoint.proceed(args);
    }

    private void cleanupCacheAsync(String key) {
        new Thread(() -> {
            try {
                Thread.sleep(20000); // 20초 후 캐시 삭제
                rotatedTokenCache.remove(key);
                log.debug("재발급 캐시 정리 완료: {}", key);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    //Access Token(JWT)을 이용해서 Spring Security의 SecurityContext를 직접 동기화하는 로직
    private void syncSecurityContext(String rawAccessToken) {
        try {
            Long userId = JwtUtils.getUserId(rawAccessToken);
            String role = JwtUtils.getRole(rawAccessToken);

            if (userId != null && role != null) {
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                Authentication auth = new UsernamePasswordAuthenticationToken(userId, null,
                        Collections.singletonList(authority));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            log.warn("SecurityContext 동기화 실패: {}", e.getMessage());
        }
    }
}