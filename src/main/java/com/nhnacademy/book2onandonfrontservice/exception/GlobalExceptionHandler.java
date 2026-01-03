package com.nhnacademy.book2onandonfrontservice.exception;

import com.nhnacademy.book2onandonfrontservice.dto.error.ErrorResponse;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
class GlobalExceptionHandler {

    /**
     * [404] 책을 찾을 수 없을때
     */
    @ExceptionHandler(NotFoundBookException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(NotFoundBookException ex) {
        log.warn("[NOT FOUND] {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}