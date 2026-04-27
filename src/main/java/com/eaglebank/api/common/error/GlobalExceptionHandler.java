package com.eaglebank.api.common.error;

import com.eaglebank.api.common.dto.BadRequestErrorResponse;
import com.eaglebank.api.common.dto.ErrorResponse;
import com.eaglebank.api.common.dto.FieldError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BadRequestErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldError> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldError(fe.getField(), fe.getDefaultMessage(), fe.getCode()))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BadRequestErrorResponse("Validation failed", details));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An unexpected error occurred"));
    }
}
