package com.veloservice.proveedores_compras.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Branch-supplier link response payload.
 */
@Data
@Builder
@AllArgsConstructor
public class SucursalProveedorResponse {
    private UUID id;
    private UUID sucursalId;
    private UUID proveedorId;
    private String codigoCliente;
    private String condicionPago;
    private String contactoAsignado;
    private Boolean activo;
    private OffsetDateTime createdAt;
}
