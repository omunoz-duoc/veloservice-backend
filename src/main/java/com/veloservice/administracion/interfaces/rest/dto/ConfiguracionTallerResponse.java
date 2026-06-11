package com.veloservice.administracion.interfaces.rest.dto;

public record ConfiguracionTallerResponse(
        String nombre,
        String rut,
        String telefono,
        String email,
        String logoUrl
) {
}
