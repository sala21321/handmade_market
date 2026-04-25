package com.example.handmademarket.exception;

import com.example.handmademarket.util.ResponseResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseResult> handleException(Exception ex) {
        return ResponseEntity.badRequest().body(ResponseResult.fail(ex.getMessage()));
    }
}
