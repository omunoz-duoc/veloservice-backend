package com.veloservice.ordenes.interfaces.rest.dto;

public record OrdenCatalogoResponse(
        String codigo,
        String nombre,
        Integer orden,
        Boolean activo
) {
}
