package com.veloservice.administracion.application.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Comando para crear una nueva sucursal. Contiene los datos necesarios para la creación de una sucursal en el sistema.
 */
@Data
@AllArgsConstructor
public class NuevaSucursalCommand {
    private UUID tallerId;
    private String nombre;
    private String direccion;
    private String telefono;
    private String email;
    private boolean activo;
}