package com.veloservice.ordenes.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class OrdenProductoUpdateCommand {
    private UUID id;
    private Integer cantidad;
    private Boolean proporcionadoPorCliente;
    private String notas;
}
