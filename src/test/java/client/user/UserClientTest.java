package client.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.nhnacademy.book2onandonfrontservice.client.UserClient;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.RestPage;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.*;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.response.*;
import feign.FeignException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class UserClientTest {

    @Mock
    private UserClient userClient;

    private final String TOKEN = "Bearer access-token";

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        LoginRequest request = new LoginRequest("user", "pass",false);
        TokenResponseDto response = mock(TokenResponseDto.class);
        given(userClient.login(request)).willReturn(response);

        TokenResponseDto result = userClient.login(request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("이메일 인증번호 전송 성공")
    void sendEmailVerification_Success() {
        userClient.sendEmailVerification("test@test.com");
        verify(userClient).sendEmailVerification("test@test.com");
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() {
        userClient.logout(TOKEN);
        verify(userClient).logout(TOKEN);
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyInfo_Success() {
        UserResponseDto response = mock(UserResponseDto.class);
        given(userClient.getMyInfo(TOKEN)).willReturn(response);

        UserResponseDto result = userClient.getMyInfo(TOKEN);

        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("내 정보 수정 성공")
    void updateMyInfo_Success() {
        UserUpdateRequest request = new UserUpdateRequest();
        userClient.updateMyInfo(TOKEN, request);
        verify(userClient).updateMyInfo(TOKEN, request);
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void withdrawUser_Success() {
        userClient.withdrawUser(TOKEN, "탈퇴사유");
        verify(userClient).withdrawUser(TOKEN, "탈퇴사유");
    }

    @Test
    @DisplayName("주소 목록 조회 성공")
    void getMyAddresses_Success() {
        List<UserAddressResponseDto> response = List.of(mock(UserAddressResponseDto.class));
        given(userClient.getMyAddresses(TOKEN)).willReturn(response);

        List<UserAddressResponseDto> result = userClient.getMyAddresses(TOKEN);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("주소 삭제 성공")
    void deleteAddress_Success() {
        userClient.deleteAddress(TOKEN, 1L);
        verify(userClient).deleteAddress(TOKEN, 1L);
    }

    @Test
    @DisplayName("닉네임 중복 확인 결과 반환")
    void checkNickname_ReturnsValue() {
        given(userClient.checkNickname("nick")).willReturn(true);
        assertThat(userClient.checkNickname("nick")).isTrue();
    }

    @Test
    @DisplayName("전체 회원 수 조회 성공")
    void getUserCount_Success() {
        ResponseEntity<Long> response = ResponseEntity.ok(100L);
        given(userClient.getUserCount(TOKEN)).willReturn(response);

        ResponseEntity<Long> result = userClient.getUserCount(TOKEN);

        assertThat(result.getBody()).isEqualTo(100L);
    }


    @Test
    @DisplayName("인증 실패 시 예외 발생")
    void login_Unauthorized() {
        given(userClient.login(any())).willThrow(FeignException.Unauthorized.class);

        assertThatThrownBy(() -> userClient.login(new LoginRequest("a", "b", false)))
                .isInstanceOf(FeignException.class);
    }
    @Test
    @DisplayName("정보 조회 시 대상을 찾을 수 없으면 예외 발생")
    void getUserDetail_NotFound() {
        given(userClient.getUserDetail(eq(TOKEN), anyLong())).willThrow(FeignException.NotFound.class);

        assertThatThrownBy(() -> userClient.getUserDetail(TOKEN, 999L))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("주소 추가 중 서버 에러 발생 시 예외 발생")
    void createAddress_ServerError() {
        doThrow(FeignException.InternalServerError.class)
                .when(userClient).createAddress(anyString(), any(UserAddressCreateRequest.class));

        assertThatThrownBy(() -> userClient.createAddress(TOKEN, new UserAddressCreateRequest()))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("비밀번호 변경 권한 거부 시 예외 발생")
    void changePassword_Forbidden() {
        doThrow(FeignException.Forbidden.class)
                .when(userClient).changePassword(anyString(), any(PasswordChangeRequest.class));

        assertThatThrownBy(() -> userClient.changePassword(TOKEN, new PasswordChangeRequest()))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("휴면 계정 해제 실패 시 예외 발생")
    void unlockDormantAccount_BadRequest() {
        doThrow(FeignException.BadRequest.class)
                .when(userClient).unlockDormantAccount(anyString(), anyString());

        assertThatThrownBy(() -> userClient.unlockDormantAccount("email", "code"))
                .isInstanceOf(FeignException.class);
    }
}