package com.veloservice.ordenes.interfaces.rest.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrdenReadResponse(
        UUID id,
        String numeroOrden,
        UUID tallerId,
        UUID sucursalId,
        CatalogoResponse estado,
        CatalogoResponse tipo,
        OffsetDateTime fechaIngreso,
        OffsetDateTime fechaPrometida,
        OffsetDateTime fechaEntrega,
        String diagnosticoInicial,
        String diagnosticoFinal,
        String observacionesCliente,
        BicicletaResponse bicicleta,
        ClienteResponse cliente,
        MecanicoResponse mecanico
) {
    public record CatalogoResponse(UUID id, String codigo, String nombre) {
    }

    public record BicicletaResponse(
            UUID id,
            String marca,
            String modelo,
            String tipo,
            String aro,
            String color,
            String numeroSerie
    ) {
    }

    public record ClienteResponse(
            UUID id,
            String nombre,
            String apellido,
            String telefono,
            String email,
            String rut
    ) {
    }

    public record MecanicoResponse(UUID id, String nombre, String apellido) {
    }
}
