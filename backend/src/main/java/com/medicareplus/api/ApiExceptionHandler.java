package com.medicareplus.api;

import java.time.Instant;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {
    record ApiError(Instant timestamp, int status, String code, String message, String path, Map<String, String> fields) {}

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> business(IllegalArgumentException ex, ServletWebRequest request) {
        return error(HttpStatus.CONFLICT, "BUSINESS_RULE_VIOLATION", ex.getMessage(), request, Map.of());
    }
    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<ApiError> notFound(NoSuchElementException ex, ServletWebRequest request) {
        return error(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Resource not found", request, Map.of());
    }
    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ApiError> credentials(ServletWebRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid credentials", request, Map.of());
    }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> forbidden(ServletWebRequest request) {
        return error(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied", request, Map.of());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, ServletWebRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> fields.put(error.getField(), error.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", request, fields);
    }
    @ExceptionHandler({ConstraintViolationException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ApiError> malformedRequest(Exception ex, ServletWebRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "The request body or parameters are invalid", request, Map.of());
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> integrity(DataIntegrityViolationException ex, ServletWebRequest request) {
        return error(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION", "The operation conflicts with persisted data", request, Map.of());
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception ex, ServletWebRequest request) {
        log.error("Unhandled API error on {}", request.getRequest().getRequestURI(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected server error occurred", request, Map.of());
    }
    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message, ServletWebRequest request, Map<String, String> fields) {
        return ResponseEntity.status(status).body(new ApiError(
            Instant.now(), status.value(), code, message, request.getRequest().getRequestURI(), fields
        ));
    }
}
