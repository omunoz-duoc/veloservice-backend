package com.veloservice.auth.application.exception;

import org.springframework.http.HttpStatus;

public class AuthException extends RuntimeException {
    private final AuthErrorCode code;

    public AuthException(AuthErrorCode code) {
        super(code.name());
        this.code = code;
    }

    public AuthErrorCode getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return code.getStatus();
    }
}
