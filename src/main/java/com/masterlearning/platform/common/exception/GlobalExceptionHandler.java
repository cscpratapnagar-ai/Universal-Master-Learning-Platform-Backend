package com.masterlearning.platform.common.exception;

import com.masterlearning.platform.common.api.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(
            BusinessException exception
    ) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(ApiErrorResponse.of(
                        exception.getErrorCode().name(),
                        exception.getMessage(),
                        Map.of()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage() == null
                            ? "Invalid value"
                            : fieldError.getDefaultMessage()
            );
        }

        return ResponseEntity.badRequest().body(
                ApiErrorResponse.of(
                        ErrorCode.VALIDATION_ERROR.name(),
                        "Request validation failed",
                        errors
                )
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {
        return ResponseEntity.badRequest().body(
                ApiErrorResponse.of(
                        ErrorCode.BAD_REQUEST.name(),
                        "Invalid value for parameter: " + exception.getName(),
                        Map.of()
                )
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException exception
    ) {
        return ResponseEntity.badRequest().body(
                ApiErrorResponse.of(
                        ErrorCode.BAD_REQUEST.name(),
                        "Malformed request body",
                        Map.of()
                )
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(
            IllegalStateException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiErrorResponse.of(
                        ErrorCode.BAD_REQUEST.name(),
                        exception.getMessage(),
                        Map.of()
                )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception
    ) {
        return ResponseEntity.badRequest().body(
                ApiErrorResponse.of(
                        ErrorCode.BAD_REQUEST.name(),
                        exception.getMessage(),
                        Map.of()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception for {} {}", request.getMethod(), request.getRequestURI(), exception);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiErrorResponse.of(
                        ErrorCode.INTERNAL_SERVER_ERROR.name(),
                        "An unexpected error occurred",
                        Map.of()
                )
        );
    }
}
