package com.veloservice.clientes.interfaces.rest.dto;

import java.util.UUID;

public record BicicletaListItem(
        UUID id,
        String marca,
        String modelo,
        String tipo,
        String color,
        String numSerie,
        Integer anio) {}
