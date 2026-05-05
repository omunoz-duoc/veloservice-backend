package com.veloservice.clientes.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Application command for creating a customer.
 */
@Data
@AllArgsConstructor
public class ClienteCreateCommand {
    private String nombre;
    private String apellido;
    private String rut;
    private String telefono;
    private String email;
    private String direccion;
}
