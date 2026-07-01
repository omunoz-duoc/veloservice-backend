package com.veloservice.auth.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record MecanicoResult(
        UUID id,
        String nombre,
        String apellido,
        String email,
        String rol,
        @JsonProperty("ordenes_activas")
        long ordenesActivas
) {
}
