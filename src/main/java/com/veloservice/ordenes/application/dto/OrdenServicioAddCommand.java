package com.veloservice.ordenes.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

/**
 * Application command for adding a service to a work order.
 */
@Data
@AllArgsConstructor
public class OrdenServicioAddCommand {
    private UUID servicioId;
    private String notas;
}
