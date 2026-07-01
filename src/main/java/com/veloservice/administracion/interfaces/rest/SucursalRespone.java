package com.veloservice.administracion.interfaces.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SucursalRespone {
    private String id;
    private String nombre;
}
