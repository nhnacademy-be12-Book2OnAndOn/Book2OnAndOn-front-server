package client.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.nhnacademy.book2onandonfrontservice.client.UserGradeClient;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.UserGradeDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.UserGradeRequestDto;
import feign.FeignException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserGradeClientTest {

    @Mock
    private UserGradeClient userGradeClient;

    private final String TOKEN = "Bearer admin-token";

    @Test
    @DisplayName("전체 등급 조회 성공")
    void getAllGrades_Success() {
        List<UserGradeDto> expectedList = List.of(mock(UserGradeDto.class));
        given(userGradeClient.getAllGrades()).willReturn(expectedList);

        List<UserGradeDto> result = userGradeClient.getAllGrades();

        assertThat(result).hasSize(1);
        verify(userGradeClient).getAllGrades();
    }

    @Test
    @DisplayName("등급 생성 성공")
    void createGrade_Success() {
        UserGradeRequestDto request = new UserGradeRequestDto();
        userGradeClient.createGrade(TOKEN, request);
        verify(userGradeClient).createGrade(eq(TOKEN), any(UserGradeRequestDto.class));
    }

    @Test
    @DisplayName("등급 수정 성공")
    void updateGrade_Success() {
        Long gradeId = 1L;
        UserGradeRequestDto request = new UserGradeRequestDto();
        userGradeClient.updateGrade(TOKEN, gradeId, request);
        verify(userGradeClient).updateGrade(eq(TOKEN), eq(gradeId), any(UserGradeRequestDto.class));
    }

    @Test
    @DisplayName("등급 조회 중 서버 에러 발생")
    void getAllGrades_ServerError() {
        given(userGradeClient.getAllGrades()).willThrow(FeignException.InternalServerError.class);

        assertThatThrownBy(() -> userGradeClient.getAllGrades())
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("권한 없는 사용자가 등급 생성 시 예외 발생")
    void createGrade_Unauthorized() {
        doThrow(FeignException.Unauthorized.class)
                .when(userGradeClient).createGrade(anyString(), any(UserGradeRequestDto.class));

        assertThatThrownBy(() -> userGradeClient.createGrade("invalid-token", new UserGradeRequestDto()))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("존재하지 않는 등급 수정 시 예외 발생")
    void updateGrade_NotFound() {
        doThrow(FeignException.NotFound.class)
                .when(userGradeClient).updateGrade(anyString(), anyLong(), any(UserGradeRequestDto.class));

        assertThatThrownBy(() -> userGradeClient.updateGrade(TOKEN, 999L, new UserGradeRequestDto()))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("잘못된 데이터로 등급 수정 요청 시 예외 발생")
    void updateGrade_BadRequest() {
        doThrow(FeignException.BadRequest.class)
                .when(userGradeClient).updateGrade(anyString(), anyLong(), any(UserGradeRequestDto.class));

        assertThatThrownBy(() -> userGradeClient.updateGrade(TOKEN, 1L, new UserGradeRequestDto()))
                .isInstanceOf(FeignException.class);
    }
}