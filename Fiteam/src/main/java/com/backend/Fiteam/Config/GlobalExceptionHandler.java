package com.backend.Fiteam.Config;

import java.util.NoSuchElementException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice    // 모든 @RestController 에 적용
public class GlobalExceptionHandler {

    // IllegalArgumentException, NoSuchElementException 은 400 Bad Request 로 응답
    @ExceptionHandler({ IllegalArgumentException.class, NoSuchElementException.class })
    public ResponseEntity<String> handleBadRequest(Exception e) {
        return ResponseEntity
                .badRequest()
                .body(e.getMessage());
    }

    // 그 외 모든 예외는 500 Internal Server Error 로 응답
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleInternalError(Exception e) {
        return ResponseEntity
                .internalServerError()
                .body(e.getMessage());
    }
}
