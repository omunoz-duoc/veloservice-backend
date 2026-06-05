package com.veloservice.ordenes.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class OrdenUpdateCommand {
    private String estadoCodigo;
    private String estadoObservacion;
    private String tipoCodigo;
    private String prioridad;
    private UUID mecanicoId;
    private List<OrdenProductoAddCommand> productosAgregar;
    private List<OrdenProductoUpdateCommand> productosActualizar;
    private List<UUID> productosEliminar;
}
