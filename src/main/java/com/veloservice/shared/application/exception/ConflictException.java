package com.veloservice.shared.application.exception;

/**
 * Requested operation conflicts with existing persisted state.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
