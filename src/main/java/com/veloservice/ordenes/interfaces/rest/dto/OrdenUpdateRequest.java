package com.veloservice.ordenes.interfaces.rest.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

import java.util.UUID;

@Data
public class OrdenUpdateRequest {
    private String estadoCodigo;
    private String estadoObservacion;
    private String tipoCodigo;
    private String prioridad;
    private UUID mecanicoId;
    @Valid
    private List<OrdenProductoCambioRequest> productosCambios;
    @Valid
    private List<OrdenProductoCambioRequest> productos;
    @Valid
    private List<OrdenServicioCambioRequest> serviciosCambios;
    @Valid
    private List<OrdenProductoAddRequest> productosAgregar;
    @Valid
    private List<OrdenProductoUpdateRequest> productosActualizar;
    private List<UUID> productosEliminar;
}
