package com.veloservice.proveedores_compras.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Application result for supplier queries.
 */
@Data
@Builder
@AllArgsConstructor
public class ProveedorResult {
    private UUID id;
    private String nombre;
    private String rut;
    private String telefono;
    private String email;
    private String direccion;
    private Boolean activo;
    private OffsetDateTime createdAt;
}
