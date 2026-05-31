package com.veloservice.administracion.application.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SucursalResult {
    UUID id;
    String nombre;
}
