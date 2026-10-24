package com.stocklms.stock_lms.exception;

import com.stocklms.stock_lms.responsebean.ResponseBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central handler for all exceptions – returns standardized ResponseEntity<ResponseBean>.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 Not Found ────────────────────────────────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseBean> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseBean.error("404", ex.getMessage(), null));
    }

    // ── 400 Business Rule Violation ──────────────────────────────────────────
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ResponseBean> handleBusinessRule(BusinessRuleException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseBean.error("400", ex.getMessage(), null));
    }

    // ── 409 Conflict ──────────────────────────────────────────────────────────
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ResponseBean> handleConflict(ConflictException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResponseBean.error("409", ex.getMessage(), null));
    }

    // ── 400 Validation errors (@Valid on request bodies) ────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseBean> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        log.warn("Validation failed: {}", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseBean.error("400", "Validation failed", fieldErrors));
    }

    // ── 400 Malformed or unreadable request body ─────────────────────────────
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseBean> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed or unreadable request body: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseBean.error("400", "Malformed or unreadable request body", null));
    }

    // ── 400 Method argument type mismatch ────────────────────────────────────
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseBean> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Method argument type mismatch for parameter '{}'", ex.getName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseBean.error("400", "Invalid argument type for parameter: " + ex.getName(), null));
    }

    // ── 405 Method not supported ─────────────────────────────────────────────
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponseBean> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("HTTP request method not supported: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ResponseBean.error("405", ex.getMessage(), null));
    }

    // ── 404 No resource found ────────────────────────────────────────────────
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ResponseBean> handleNoResourceFound(NoResourceFoundException ex) {
        log.warn("No resource found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseBean.error("404", ex.getMessage(), null));
    }

    // ── 409 Data integrity violation (generic message, no SQL text) ───────────
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseBean> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResponseBean.error("409", "Data integrity violation occurred. The requested operation conflicts with existing database constraints.", null));
    }

    // ── 500 Catch-all ────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseBean> handleUnexpected(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseBean.error("500", "An unexpected error occurred. Please try again later.", null));
    }
}
