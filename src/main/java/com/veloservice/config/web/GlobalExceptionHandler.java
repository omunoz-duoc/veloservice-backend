package com.veloservice.config.web;

import com.veloservice.auth.application.exception.AuthException;
import com.veloservice.inventario.application.exception.ProductoException;
import com.veloservice.shared.application.exception.BadRequestException;
import com.veloservice.shared.application.exception.ConflictException;
import com.veloservice.shared.application.exception.ResourceNotFoundException;
import com.veloservice.shared.application.exception.ServiceUnavailableException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.time.OffsetDateTime;
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
        body.put("timestamp", OffsetDateTime.now());
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
                message = "UUID invalido (formato: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx)";
            } else if (causeMsg.contains("cannot deserialize")) {
                message = "Formato de dato invalido. Verifica los tipos de campo.";
            } else if (causeMsg.contains("unexpected character") || causeMsg.contains("expected double-quote")) {
                message = "JSON invalido. Verifica la sintaxis (comillas, comas y llaves).";
            }
        }

        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return buildResponse(status, message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Acceso denegado";
        return buildResponse(HttpStatus.FORBIDDEN, message);
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class})
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(Exception ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Validacion fallida");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName() != null ? ex.getName() : "parametro";
        return buildResponse(HttpStatus.BAD_REQUEST, name + " tiene un formato invalido");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(MissingServletRequestParameterException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Parametro requerido faltante: " + ex.getParameterName());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Map<String, Object>> handleMissingPart(MissingServletRequestPartException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Parte requerida faltante: " + ex.getRequestPartName());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE, "Archivo demasiado grande");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de contenido no soportado");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({
            ServiceUnavailableException.class,
            DataAccessResourceFailureException.class,
            CannotGetJdbcConnectionException.class,
            QueryTimeoutException.class,
            ResourceAccessException.class
    })
    public ResponseEntity<Map<String, Object>> handleServiceUnavailable(Exception ex) {
        log.warn("Recurso requerido no disponible", ex);
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "Servicio temporalmente no disponible");
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
        if (isUniqueConstraintViolation(cause)) {
            return buildResponse(HttpStatus.CONFLICT, "La solicitud entra en conflicto con datos existentes");
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
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    private boolean isUniqueConstraintViolation(String cause) {
        if (cause == null) {
            return false;
        }
        String normalized = cause.toLowerCase();
        return normalized.contains("duplicate")
                || normalized.contains("unique")
                || normalized.contains("uk_");
    }
}
