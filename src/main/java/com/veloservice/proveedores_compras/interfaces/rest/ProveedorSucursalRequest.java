package com.veloservice.proveedores_compras.interfaces.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * Branch-provider assignment payload.
 */
@Data
public class ProveedorSucursalRequest {
    @NotNull
    private UUID proveedorId;
    @NotBlank
    private String codigoCliente;
    @NotBlank
    private String condicionPago;
    private String contactoAsignado;
}