package com.veloservice.ordenes.interfaces.rest;

import com.veloservice.ordenes.domain.EstadoOrdenEnum;
import com.veloservice.ordenes.domain.TipoOrdenEnum;

import java.time.OffsetDateTime;

public record OrdenResumenResponse(
        String externalId,
        TipoOrdenEnum tipo,
        OffsetDateTime fechaIngreso,
        String nombreMecanico,
        String nombreCliente,
        String descripcion,
        String observacionesCliente,
        BicicletaResumenResponse bicicleta,
        EstadoOrdenEnum estado
) {
    public record BicicletaResumenResponse(
            String marca,
            String color,
            String tipo,
            String talla
    ) {
    }
}