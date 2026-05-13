package com.veloservice.ordenes.interfaces.rest;

import java.util.UUID;

public record MecanicoResponse(
        UUID id,
        String nombre,
        String apellido,
        String iniciales,
        String email,
        Boolean activo,
        UUID sucursalId
) {
}
