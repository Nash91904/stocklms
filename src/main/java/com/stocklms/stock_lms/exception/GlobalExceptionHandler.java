package com.stocklms.stock_lms.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central handler for all exceptions – returns consistent JSON error bodies.
 * Uses Spring 6's ProblemDetail (RFC 7807) for structured error responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 Not Found ────────────────────────────────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setType(URI.create("urn:stocklms:error:not-found"));
        pd.setTitle("Resource Not Found");
        pd.setDetail(ex.getMessage());
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // ── 400 Business Rule Violation ──────────────────────────────────────────
    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setType(URI.create("urn:stocklms:error:business-rule"));
        pd.setTitle("Business Rule Violated");
        pd.setDetail(ex.getMessage());
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // ── 409 Conflict ──────────────────────────────────────────────────────────
    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setType(URI.create("urn:stocklms:error:conflict"));
        pd.setTitle("Conflict");
        pd.setDetail(ex.getMessage());
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // ── 400 Validation errors (@Valid on request bodies) ────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        // Collect all field-level errors into a map: fieldName → message
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        log.warn("Validation failed: {}", fieldErrors);
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setType(URI.create("urn:stocklms:error:validation"));
        pd.setTitle("Validation Failed");
        pd.setDetail("One or more fields are invalid.");
        pd.setProperty("fieldErrors", fieldErrors);
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // ── 500 Catch-all ────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error occurred", ex);
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setType(URI.create("urn:stocklms:error:internal"));
        pd.setTitle("Internal Server Error");
        pd.setDetail("An unexpected error occurred. Please try again later.");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}
