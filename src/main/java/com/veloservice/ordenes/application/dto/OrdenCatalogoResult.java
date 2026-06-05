package com.veloservice.ordenes.application.dto;

public record OrdenCatalogoResult(
        String codigo,
        String nombre,
        Integer orden,
        Boolean activo
) {
}
