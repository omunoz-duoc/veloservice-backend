package com.veloservice.proveedores_compras.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Supplier response payload.
 */
@Data
@Builder
@AllArgsConstructor
public class ProveedorResponse {
    private UUID id;
    private String nombre;
    private String rut;
    private String telefono;
    private String email;
    private String direccion;
    private Boolean activo;
    private OffsetDateTime createdAt;
}
