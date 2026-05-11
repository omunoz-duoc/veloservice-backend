package com.veloservice.administracion.application.exception;

import org.springframework.http.HttpStatus;

/**
 * Business exception for Google authentication.
 */
public class GoogleAuthException extends RuntimeException {
    private final GoogleAuthErrorCode code;

    public GoogleAuthException(GoogleAuthErrorCode code) {
        super(code.name());
        this.code = code;
    }

    public GoogleAuthErrorCode getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return code.getStatus();
    }
}
