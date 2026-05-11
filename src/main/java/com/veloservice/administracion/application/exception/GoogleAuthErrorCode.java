package com.veloservice.administracion.application.exception;

import org.springframework.http.HttpStatus;

/**
 * Error codes for Google authentication.
 */
public enum GoogleAuthErrorCode {
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED),
    EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND),
    SUCURSAL_NOT_FOUND(HttpStatus.NOT_FOUND),
    USER_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    GoogleAuthErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
