package client.wrapping;

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

import com.nhnacademy.book2onandonfrontservice.client.WrappingPaperAdminClient;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.request.WrappingPaperRequestDto;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.WrappingPaperResponseDto;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class WrappingPaperAdminClientTest {

    @Mock
    private WrappingPaperAdminClient wrappingPaperAdminClient;

    private final String TOKEN = "Bearer admin-token";

    @Test
    @DisplayName("포장지 등록 성공")
    void createWrappingPaper_Success() {
        WrappingPaperRequestDto requestDto = mock(WrappingPaperRequestDto.class);
        WrappingPaperResponseDto responseDto = mock(WrappingPaperResponseDto.class);
        given(wrappingPaperAdminClient.createWrappingPaper(TOKEN, requestDto)).willReturn(responseDto);

        WrappingPaperResponseDto result = wrappingPaperAdminClient.createWrappingPaper(TOKEN, requestDto);

        assertThat(result).isEqualTo(responseDto);
        verify(wrappingPaperAdminClient).createWrappingPaper(TOKEN, requestDto);
    }

    @Test
    @DisplayName("관리자용 포장지 전체 목록 조회 성공")
    void getAllWrappingPapers_Success() {
        @SuppressWarnings("unchecked")
        Page<WrappingPaperResponseDto> expectedPage = mock(Page.class);
        Pageable pageable = PageRequest.of(0, 10);
        given(wrappingPaperAdminClient.getAllWrappingPapers(TOKEN, pageable)).willReturn(expectedPage);

        Page<WrappingPaperResponseDto> result = wrappingPaperAdminClient.getAllWrappingPapers(TOKEN, pageable);

        assertThat(result).isEqualTo(expectedPage);
    }

    @Test
    @DisplayName("포장지 단건 상세 조회 성공")
    void getWrappingPaper_Success() {
        Long wrappingPaperId = 1L;
        WrappingPaperResponseDto responseDto = mock(WrappingPaperResponseDto.class);
        given(wrappingPaperAdminClient.getWrappingPaper(TOKEN, wrappingPaperId)).willReturn(responseDto);

        WrappingPaperResponseDto result = wrappingPaperAdminClient.getWrappingPaper(TOKEN, wrappingPaperId);

        assertThat(result).isEqualTo(responseDto);
    }

    @Test
    @DisplayName("포장지 정보 수정 성공")
    void updateWrappingPaper_Success() {
        Long wrappingPaperId = 1L;
        WrappingPaperResponseDto responseDto = mock(WrappingPaperResponseDto.class);
        given(wrappingPaperAdminClient.updateWrappingPaper(TOKEN, wrappingPaperId)).willReturn(responseDto);

        WrappingPaperResponseDto result = wrappingPaperAdminClient.updateWrappingPaper(TOKEN, wrappingPaperId);

        assertThat(result).isEqualTo(responseDto);
    }

    @Test
    @DisplayName("포장지 삭제 성공")
    void deleteWrappingPaper_Success() {
        Long wrappingPaperId = 1L;
        wrappingPaperAdminClient.deleteWrappingPaper(TOKEN, wrappingPaperId);
        verify(wrappingPaperAdminClient).deleteWrappingPaper(TOKEN, wrappingPaperId);
    }

    @Test
    @DisplayName("권한 없이 포장지 등록 시 예외 발생")
    void createWrappingPaper_Unauthorized() {
        given(wrappingPaperAdminClient.createWrappingPaper(anyString(), any()))
                .willThrow(FeignException.Unauthorized.class);

        assertThatThrownBy(() -> wrappingPaperAdminClient.createWrappingPaper("invalid", new WrappingPaperRequestDto()))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("존재하지 않는 포장지 조회 시 예외 발생")
    void getWrappingPaper_NotFound() {
        given(wrappingPaperAdminClient.getWrappingPaper(anyString(), anyLong()))
                .willThrow(FeignException.NotFound.class);

        assertThatThrownBy(() -> wrappingPaperAdminClient.getWrappingPaper(TOKEN, 999L))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("포장지 삭제 중 서버 에러 발생 시 예외 발생")
    void deleteWrappingPaper_ServerError() {
        doThrow(FeignException.InternalServerError.class)
                .when(wrappingPaperAdminClient).deleteWrappingPaper(anyString(), anyLong());

        assertThatThrownBy(() -> wrappingPaperAdminClient.deleteWrappingPaper(TOKEN, 1L))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("수정 요청 시 잘못된 매개변수로 인한 예외 발생")
    void updateWrappingPaper_BadRequest() {
        given(wrappingPaperAdminClient.updateWrappingPaper(any(), anyLong()))
                .willThrow(FeignException.BadRequest.class);

        assertThatThrownBy(() -> wrappingPaperAdminClient.updateWrappingPaper(TOKEN, 1L))
                .isInstanceOf(FeignException.class);
    }
}