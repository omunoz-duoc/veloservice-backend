package com.veloservice.shared.application.exception;

/**
 * Client request is syntactically valid but violates an API/business precondition.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
