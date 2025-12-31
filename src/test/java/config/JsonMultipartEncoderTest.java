package config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.book2onandonfrontservice.config.JsonMultipartEncoder;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookSaveRequest;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.form.FormData;
import feign.form.spring.SpringFormEncoder;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JsonMultipartEncoderTest {

    @Mock
    private SpringFormEncoder delegate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JsonMultipartEncoder jsonMultipartEncoder;

    @Test
    @DisplayName("DTO 객체가 포함된 맵 데이터를 JSON FormData로 변환하여 인코딩")
    void encode_WithDtoInMap() throws JsonProcessingException {
        BookSaveRequest bookRequest = mock(BookSaveRequest.class);
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("book", bookRequest);
        objectMap.put("other", "value");

        byte[] jsonBytes = "{\"title\":\"test\"}".getBytes();
        given(objectMapper.writeValueAsBytes(bookRequest)).willReturn(jsonBytes);

        jsonMultipartEncoder.encode(objectMap, Map.class, new RequestTemplate());

        verify(delegate).encode(any(Map.class), eq(Map.class), any(RequestTemplate.class));
        verify(objectMapper).writeValueAsBytes(bookRequest);
    }

    @Test
    @DisplayName("DTO가 아닌 객체만 포함된 맵 데이터 인코딩")
    void encode_WithNonDtoInMap() {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("key", "value");

        jsonMultipartEncoder.encode(objectMap, Map.class, new RequestTemplate());

        verify(delegate).encode(eq(objectMap), eq(Map.class), any(RequestTemplate.class));
    }

    @Test
    @DisplayName("맵이 아닌 일반 객체 데이터 인코딩")
    void encode_WithNonMapObject() {
        String simpleObject = "test";

        jsonMultipartEncoder.encode(simpleObject, String.class, new RequestTemplate());

        verify(delegate).encode(eq(simpleObject), eq(String.class), any(RequestTemplate.class));
    }

    @Test
    @DisplayName("JSON 변환 중 에러 발생 시 EncodeException 발생")
    void encode_ThrowsEncodeException() throws JsonProcessingException {
        BookSaveRequest bookRequest = mock(BookSaveRequest.class);
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("book", bookRequest);

        given(objectMapper.writeValueAsBytes(any())).willThrow(JsonProcessingException.class);

        assertThatThrownBy(() -> jsonMultipartEncoder.encode(objectMap, Map.class, new RequestTemplate()))
                .isInstanceOf(EncodeException.class)
                .hasMessageContaining("DTO를 JSON으로 변환할 수 없습니다");
    }
}