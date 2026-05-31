package com.veloservice.administracion.application.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TallerResult {
    UUID id;
    UUID planId;
    String nombre;
    String rut;
    String telefono;
    String email;
    String logoUrl;
    boolean activo;
}
