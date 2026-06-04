package com.veloservice.auth.application.dto;

import java.util.UUID;

public record MecanicoResult(
        UUID id,
        String nombre,
        String apellido,
        String email,
        String rol
) {
}
