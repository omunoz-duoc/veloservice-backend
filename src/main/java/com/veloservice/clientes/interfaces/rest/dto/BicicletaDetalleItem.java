package com.veloservice.clientes.interfaces.rest.dto;

import java.util.UUID;

/**
 * REST DTO for a bike item inside the customer detail response.
 */
public record BicicletaDetalleItem(
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
