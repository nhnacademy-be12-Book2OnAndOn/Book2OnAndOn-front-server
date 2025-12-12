package com.nhnacademy.book2onandonfrontservice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookSaveRequest;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookUpdateRequest;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import feign.form.FormData;
import feign.form.spring.SpringFormEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class JsonMultipartEncoder implements Encoder {

    private final SpringFormEncoder delegate;
    private final ObjectMapper objectMapper;

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        if (object instanceof Map) {
            Map<String, Object> data = (Map<String, Object>) object;
            Map<String, Object> newMap = new HashMap<>();

            for (Map.Entry<String, Object> entry : data.entrySet()) {
                Object value = entry.getValue();

                if (isDto(value)) {
                    try {
                        // JSON을 바이트로 변환
                        byte[] jsonBytes = objectMapper.writeValueAsBytes(value);

                        // MockMultipartFile로 감싸서 application/json 타입 지정
                        FormData formData = FormData.builder()
                                .fileName("data.json")
                                .contentType("application/json")
                                .data(jsonBytes)
                                .build();


                        newMap.put(entry.getKey(), formData);
                        log.debug("Converted DTO to JSON file: {} bytes", jsonBytes.length);
                    } catch (JsonProcessingException e) {
                        log.error("JSON 변환 실패", e);
                        throw new EncodeException("DTO를 JSON으로 변환할 수 없습니다", e);
                    }
                } else {
                    newMap.put(entry.getKey(), value);
                }
            }

            delegate.encode(newMap, bodyType, template);
            return;
        }

        delegate.encode(object, bodyType, template);
    }

    private boolean isDto(Object object) {
        return object instanceof BookSaveRequest || object instanceof BookUpdateRequest;
    }
}