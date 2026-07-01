package com.veloservice.ordenes.interfaces.rest.dto;

import java.time.OffsetDateTime;

public record OrdenResumenResponse(
    String numeroOrden,
    String tipo,
    OffsetDateTime fechaIngreso,
    String mecanico,
    String cliente,
    BicicletaResumenResponse bicicleta,
    String diagnosticoInicial,
    String estado,
    String prioridad
) {
    public record BicicletaResumenResponse(
        String marca, String modelo, String tipo, String color
    ) {}
}
