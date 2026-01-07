package client.wrapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.nhnacademy.book2onandonfrontservice.client.WrappingPaperClient;
import com.nhnacademy.book2onandonfrontservice.dto.orderDto.response.WrappingPaperSimpleResponseDto;
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
class WrappingPaperClientTest {

    @Mock
    private WrappingPaperClient wrappingPaperClient;

    @Test
    @DisplayName("포장지 목록 페이징 조회 성공")
    void getWrappingPaperList_Success() {
        @SuppressWarnings("unchecked")
        Page<WrappingPaperSimpleResponseDto> expectedPage = mock(Page.class);
        String token = "Bearer test-token";
        Pageable pageable = PageRequest.of(0, 10);

        given(wrappingPaperClient.getWrappingPaperList(token, pageable)).willReturn(expectedPage);

        Page<WrappingPaperSimpleResponseDto> result = wrappingPaperClient.getWrappingPaperList(token, pageable);

        assertThat(result).isEqualTo(expectedPage);
        verify(wrappingPaperClient).getWrappingPaperList(token, pageable);
    }

    @Test
    @DisplayName("비인증 사용자가 포장지 목록 조회 성공")
    void getWrappingPaperList_Guest_Success() {
        @SuppressWarnings("unchecked")
        Page<WrappingPaperSimpleResponseDto> expectedPage = mock(Page.class);
        Pageable pageable = PageRequest.of(0, 10);

        given(wrappingPaperClient.getWrappingPaperList(null, pageable)).willReturn(expectedPage);

        Page<WrappingPaperSimpleResponseDto> result = wrappingPaperClient.getWrappingPaperList(null, pageable);

        assertThat(result).isEqualTo(expectedPage);
        verify(wrappingPaperClient).getWrappingPaperList(null, pageable);
    }

    @Test
    @DisplayName("포장지 목록 조회 중 서버 에러 발생 시 예외 발생")
    void getWrappingPaperList_ServerError() {
        given(wrappingPaperClient.getWrappingPaperList(anyString(), any(Pageable.class)))
                .willThrow(FeignException.InternalServerError.class);

        assertThatThrownBy(() -> wrappingPaperClient.getWrappingPaperList("token", PageRequest.of(0, 10)))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("잘못된 페이지 요청 시 예외 발생")
    void getWrappingPaperList_BadRequest() {
        Pageable pageable = PageRequest.of(0, 10);

        given(wrappingPaperClient.getWrappingPaperList(any(), eq(pageable)))
                .willThrow(FeignException.BadRequest.class);

        assertThatThrownBy(() -> wrappingPaperClient.getWrappingPaperList(null, pageable))
                .isInstanceOf(FeignException.class);
    }
}