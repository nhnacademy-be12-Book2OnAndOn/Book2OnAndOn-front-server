package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.FindIdRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.FindPasswordRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.LocalSignUpRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.LoginRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.FindIdResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.TokenResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "gateway-service", url = "${gateway.base-url}")
public interface UserClient {

    // [Auth] 로그인
    @PostMapping("/api/auth/login")
    TokenResponseDto login(@RequestBody LoginRequest request);


    // [Auth] 회원가입
    @PostMapping("/api/auth/signup")
    void signUp(@RequestBody LocalSignUpRequest request);

    /*
     * [User] 내 정보 조회 (마이페이지용)
     * Gateway가 헤더를 넣어주지만, 프론트->Gateway 호출 시에는
     * 프론트가 직접 쿠키에서 꺼낸 토큰을 헤더에 넣어서 보내야 합니다.
     */
    @GetMapping("/api/users/me")
    UserResponseDto getMyInfo(@RequestHeader("Authorization") String accessToken);

    // [User] 아이디 찾기
    @PostMapping("/api/auth/find-id")
    FindIdResponseDto findId(@RequestBody FindIdRequest request);

    // [User] 임시비밀번호 발급

    @PostMapping("/api/auth/find-password")
    void findPassword(@RequestBody FindPasswordRequest request);


}
