package com.nhnacademy.book2onandonfrontservice.tokenreissue;

import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.ReissueRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.TokenResponseDto;
import com.nhnacademy.book2onandonfrontservice.service.FrontTokenService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenReissueAspect {

    private final UserClient userClient;
    private final FrontTokenService tokenService;

    @Around("execution(* com.nhnacademy.book2onandonfrontservice.client..*(..))")
    public Object handle401(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (FeignException.Unauthorized e) {

            // 로그인 실패나 재발급 요청 자체의 실패는 재시도하지 않음 (무한루프 방지)
            String methodName = joinPoint.getSignature().getName();
            if (methodName.equals("login") || methodName.equals("reissue")) {
                throw e;
            }

            log.info("Access Token 만료 감지(401). 재발급을 시도합니다.");

            try {
                String refreshToken = tokenService.getRefreshToken();
                if (refreshToken == null) {
                    log.error("Refresh Token이 없어 재발급 불가");
                    throw e;
                }

                // 3. 토큰 재발급 요청
                TokenResponseDto newToken = userClient.reissue(new ReissueRequestDto(null, refreshToken));

                // 4. 쿠키 갱신
                tokenService.updateTokens(newToken);

                // 5. 새 토큰으로 인자(Args) 교체
                Object[] args = joinPoint.getArgs();
                for (int i = 0; i < args.length; i++) {
                    // "Bearer "로 시작하는 문자열 인자가 있다면 AccessToken으로 간주하고 교체
                    if (args[i] instanceof String && ((String) args[i]).startsWith("Bearer ")) {
                        String newAccessToken = "Bearer " + newToken.getAccessToken();
                        args[i] = newAccessToken;
                        log.info("인자의 토큰을 새 토큰으로 교체했습니다.");
                    }
                }

                log.info("토큰 재발급 성공. 변경된 토큰으로 재요청합니다.");

                return joinPoint.proceed(args);

            } catch (Exception reissueEx) {
                log.error("토큰 재발급 과정 실패: {}", reissueEx.getMessage());
                throw e;
            }
        }
    }
}