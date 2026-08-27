package com.bagusxmahendra.mltf.case_management_service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        log.warn("ResponseStatusException: status={}, reason={}", ex.getStatusCode(), ex.getReason());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", ex.getStatusCode().value());
        body.put("error", ex.getReason() != null ? ex.getReason() : ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(WebExchangeBindException ex) {
        log.warn("Validation error on request: {}", ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getFieldErrors().forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));
        body.put("error", "Validation failed");
        body.put("details", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<Map<String, Object>> handleInputException(ServerWebInputException ex) {
        log.warn("ServerWebInputException: {}", ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", ex.getReason() != null ? ex.getReason() : ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unhandled exception processing request: {}", ex.getMessage(), ex);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal server error: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
