package com.veloservice.inventario.application.exception;

/**
 * Excepción de negocio para inventario.
 */
public class ProductoException extends RuntimeException {
    private final ProductoErrorCode code;

    public ProductoException(ProductoErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ProductoErrorCode getCode() {
        return code;
    }
}
