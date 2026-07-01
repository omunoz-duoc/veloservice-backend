package com.veloservice.auth.application.exception;

import org.springframework.http.HttpStatus;

public enum AuthErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED),
    ACCOUNT_NOT_VERIFIED(HttpStatus.FORBIDDEN),
    TOO_MANY_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS),
    USUARIO_SIN_SUCURSAL_PRINCIPAL(HttpStatus.FORBIDDEN),
    SUCURSAL_NO_PERTENECE_TALLER(HttpStatus.FORBIDDEN),
    AMBITO_ROL_INVALIDO(HttpStatus.FORBIDDEN);

    private final HttpStatus status;

    AuthErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
