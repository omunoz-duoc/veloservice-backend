package com.veloservice.proveedores_compras.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Application result for branch-supplier links.
 */
@Data
@Builder
@AllArgsConstructor
public class SucursalProveedorResult {
    private UUID id;
    private UUID sucursalId;
    private UUID proveedorId;
    private String codigoCliente;
    private String condicionPago;
    private String contactoAsignado;
    private Boolean activo;
    private OffsetDateTime createdAt;
}
