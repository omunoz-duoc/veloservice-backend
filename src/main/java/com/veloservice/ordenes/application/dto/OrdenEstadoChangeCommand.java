package com.veloservice.ordenes.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Application command for changing a work order state.
 */
@Data
@AllArgsConstructor
public class OrdenEstadoChangeCommand {
    private String codigo;
    private String observacion;
}
