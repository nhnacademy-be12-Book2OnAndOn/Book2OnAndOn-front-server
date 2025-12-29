package com.nhnacademy.book2onandonfrontservice;

import feign.FeignException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 기능동작에는 필요없지만 FeignException은 Spring의 기본 예외 처리로 넘어가기 때문에 원활한 디버깅을 위해 추가함
// 디버깅 다하고 나서 지워도 상관없음
@RestControllerAdvice
public class FeignExceptionAdvice {

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<String> handleFeign(FeignException e) {
        int status = e.status() > 0 ? e.status() : 502;
        String body = e.contentUTF8();
        return ResponseEntity.status(status).body(body);
    }
}
