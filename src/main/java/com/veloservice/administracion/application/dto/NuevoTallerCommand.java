package com.veloservice.administracion.application.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NuevoTallerCommand {
    private UUID planId;
    private String nombre;
    private String rut;
    private String telefono;
    private String email;
    private String logoUrl;
}
