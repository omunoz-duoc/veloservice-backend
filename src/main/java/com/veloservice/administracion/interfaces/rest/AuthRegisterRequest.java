package com.veloservice.administracion.interfaces.rest;

import lombok.Data;

import java.util.UUID;

/**
 * Registration request payload.
 */
@Data
public class AuthRegisterRequest {
    private String nombre;
    private String apellido;
    private String rut;
    private String telefono;
    private String email;
    private String password;
    private UUID sucursalId;
    private String rol;
}
