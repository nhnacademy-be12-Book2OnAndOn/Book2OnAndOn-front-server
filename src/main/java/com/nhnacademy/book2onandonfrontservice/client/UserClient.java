package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.userDto.RestPage;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.AdminUserUpdateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.FindIdRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.FindPasswordRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.LocalSignUpRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.LoginRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.PasswordChangeRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.UserAddressCreateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.UserAddressUpdateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.UserUpdateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.BookReviewResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.FindIdResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.TokenResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.UserAddressResponseDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.UserResponseDto;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "gateway-service", contextId = "userClient", url = "${gateway.base-url}")
public interface UserClient {

    // [Auth] 로그인
    @PostMapping("/api/auth/login")
    TokenResponseDto login(@RequestBody LoginRequest request);


    // [Auth] 회원가입
    @PostMapping("/api/auth/signup")
    void signUp(@RequestBody LocalSignUpRequest request);

    //[Auth] 이메일 인증번호 전송
    @PostMapping("/api/auth/email/send")
    void sendEmailVerification(@RequestParam("email") String email);

    //[Auth] 이메일 인증번호 검증
    @PostMapping("/api/auth/email/verify")
    String verifyEmail(@RequestParam("email") String email, @RequestParam("code") String code);

    //[Auth] 로그아웃
    @PostMapping("/api/auth/logout")
    void logout(@RequestHeader("Authorization") String accessToken);

    // [Auth] 아이디 찾기
    @PostMapping("/api/auth/find-id")
    FindIdResponseDto findId(@RequestBody FindIdRequest request);

    // [Auth] 임시비밀번호 발급
    @PostMapping("/api/auth/find-password")
    void findPassword(@RequestBody FindPasswordRequest request);

    /*
     * [User] 내 정보 조회 (마이페이지용)
     * Gateway가 헤더를 넣어주지만, 프론트->Gateway 호출 시에는
     * 프론트가 직접 쿠키에서 꺼낸 토큰을 헤더에 넣어서 보내야함.
     */
    @GetMapping("/api/users/me")
    UserResponseDto getMyInfo(@RequestHeader("Authorization") String accessToken);

    // [User] 특정 회원의 리뷰 목록 조회
    @GetMapping("/api/users/{userId}/reviews")
    RestPage<BookReviewResponseDto> getUserReviews(
            @PathVariable("userId") Long userId,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );

    // [User] 내 정보 수정
    @PutMapping("/api/users/me")
    void updateMyInfo(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody UserUpdateRequest request
    );

    // [User] 비밀번호 변경
    @PutMapping("/api/users/me/password")
    void changePassword(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody PasswordChangeRequest request
    );

    //[Admin] 전체 회원 목록 조회
    @GetMapping("/api/admin/users")
    RestPage<UserResponseDto> getUsers(
            @RequestHeader("Authorization") String accessToken,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );

    //[Admin] 회원 상세 조회
    @GetMapping("/api/admin/users/{userId}")
    UserResponseDto getUserDetail(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("userId") Long userId
    );

    //[Admin] 회원 정보 수정
    @PutMapping("/api/admin/users/{userId}")
    void updateUser(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("userId") Long userId,
            @RequestBody AdminUserUpdateRequest request
    );

    //[Admin] 회원 강제 탈퇴
    @DeleteMapping("/api/admin/users/{userId}")
    void deleteUser(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("userId") Long userId,
            @RequestParam("reason") String reason
    );

    //[Address] 주소 목록 조회
    @GetMapping("/api/users/me/addresses")
    List<UserAddressResponseDto> getMyAddresses(@RequestHeader("Authorization") String accessToken);

    //[Address] 주소 상세조회
    @GetMapping("/api/users/me/addresses/{addressId}")
    UserAddressResponseDto getAddressDetail(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("addressId") Long addressId
    );

    //[Address] 주소 추가
    @PostMapping("/api/users/me/addresses")
    void createAddress(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody UserAddressCreateRequest request
    );

    //[Address] 주소 수정
    @PutMapping("/api/users/me/addresses/{addressId}")
    void updateAddress(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("addressId") Long addressId,
            @RequestBody UserAddressUpdateRequest request
    );

    //[Address] 주소 삭제
    @DeleteMapping("/api/users/me/addresses/{addressId}")
    void deleteAddress(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("addressId") Long addressId
    );
}
