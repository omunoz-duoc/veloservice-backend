package com.veloservice.administracion.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TallerResponse {
    private String id;
    private String rut;
    private String nombre;
    private String telefono;
    private String email;
    private String logoUrl;
    private String planId;
    private boolean activo;
}
