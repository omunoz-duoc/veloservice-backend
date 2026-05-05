package com.veloservice.proveedores_compras.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

/**
 * Application command for assigning a supplier to a branch.
 */
@Data
@AllArgsConstructor
public class SucursalProveedorCreateCommand {
    private UUID proveedorId;
    private String codigoCliente;
    private String condicionPago;
    private String contactoAsignado;
}
