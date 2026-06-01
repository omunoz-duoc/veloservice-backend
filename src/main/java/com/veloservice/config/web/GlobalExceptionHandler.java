package com.veloservice.config.web;

import com.veloservice.auth.application.exception.AuthException;
import com.veloservice.inventario.application.exception.ProductoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized error handling for REST controllers.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * Handles authentication errors with explicit status codes.
     *
     * @param ex auth exception
     * @return error response
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, Object>> handleAuthException(AuthException ex) {
        return buildResponse(ex.getStatus(), ex.getCode().name());
    }

    /**
     * Handles business logic errors from inventory.
     *
     * @param ex product exception
     * @return error response
     */
    @ExceptionHandler(ProductoException.class)
    public ResponseEntity<Map<String, Object>> handleProductoException(ProductoException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Handles invalid credential errors.
     *
     * @param ex bad credentials exception
     * @return error response
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
    }

    /**
     * Handles validation errors for request payloads.
     *
     * @param ex validation exception
     * @return error response with field details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put("message", "Validacion fallida");
        body.put("errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handles malformed JSON payloads and field deserialization errors.
     *
     * @param ex message not readable exception
     * @return error response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJson(HttpMessageNotReadableException ex) {
        String message = "JSON invalido";
        Throwable cause = ex.getCause();

        if (cause != null && cause.getMessage() != null) {
            String causeMsg = cause.getMessage().toLowerCase();
            if (causeMsg.contains("uuid")) {
                message = "sucursalId debe ser un UUID valido (formato: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx)";
            } else if (causeMsg.contains("cannot deserialize")) {
                message = "Formato de dato invalido. Verifica los tipos de campo.";
            } else if (causeMsg.contains("unexpected character") || causeMsg.contains("expected double-quote")) {
                message = "JSON invalido. Verifica la sintaxis (comillas, comas y llaves).";
            }
        }

        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Handles business logic errors from service layer.
     *
     * @param ex illegal argument exception
     * @return error response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String cause = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "";
        if (cause != null && cause.contains("idx_ordenes_taller_numero")) {
            return buildResponse(HttpStatus.CONFLICT, "Numero de orden duplicado. Intenta crear la orden nuevamente.");
        }
        return buildResponse(HttpStatus.BAD_REQUEST, "La solicitud viola una restriccion de datos");
    }

    /**
     * Handles unexpected errors.
     *
     * @param ex exception
     * @return error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Error no controlado", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
