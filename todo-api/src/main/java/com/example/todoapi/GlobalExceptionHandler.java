package com.example.todoapi;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        Map<String,Object> body = Map.of(
            "timestamp", LocalDateTime.now(),
            "status",    HttpStatus.BAD_REQUEST.value(),
            "error",     "Bad Request",
            "message",   ex.getMessage()
            );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
