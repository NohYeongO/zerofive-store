package com.zerofive.store.core.exception;

import com.zerofive.store.core.response.ApiResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Void>> handleBusinessException(BusinessException e) {
        return ResponseEntity
                .status(e.getCode())
                .body(ApiResult.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(StockException.class)
    public ResponseEntity<ApiResult<Void>> handleStockException(StockException e) {
        String message = String.format("[%s] %s (productId: %d)",
                e.getErrorCode(), e.getMessage(), e.getProductId());
        return ResponseEntity
                .badRequest()
                .body(ApiResult.error(400, message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Validation failed");

        return ResponseEntity
                .badRequest()
                .body(ApiResult.error(400, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleException(Exception e) {
        return ResponseEntity
                .internalServerError()
                .body(ApiResult.error(500, "Internal server error"));
    }
}
