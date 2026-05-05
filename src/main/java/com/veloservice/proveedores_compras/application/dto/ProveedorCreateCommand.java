package com.veloservice.proveedores_compras.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Application command for creating a supplier.
 */
@Data
@AllArgsConstructor
public class ProveedorCreateCommand {
    private String nombre;
    private String rut;
    private String telefono;
    private String email;
    private String direccion;
    private String condicionPago;
    private String contactoAsignado;
}
