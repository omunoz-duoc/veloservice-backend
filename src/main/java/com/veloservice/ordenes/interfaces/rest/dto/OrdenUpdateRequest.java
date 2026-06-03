package com.veloservice.ordenes.interfaces.rest.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class OrdenUpdateRequest {
    private String estadoCodigo;
    private String estadoObservacion;
    private String tipoCodigo;
    private String prioridad;
    private UUID mecanicoId;
}
