package com.veloservice.clientes.application.dto;

import java.util.UUID;

/**
 * Application-layer result carrying bike detail for a customer profile.
 */
public record BicicletaDetalleResult(
        UUID id,
        String marca,
        String modelo,
        String tipo,
        String aro,
        String color,
        String numeroSerie,
        Integer anio,
        String notas
) {
}
