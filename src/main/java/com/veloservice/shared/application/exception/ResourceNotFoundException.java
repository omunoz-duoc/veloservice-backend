package com.veloservice.shared.application.exception;

/**
 * Requested resource was not found in the current API scope.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
