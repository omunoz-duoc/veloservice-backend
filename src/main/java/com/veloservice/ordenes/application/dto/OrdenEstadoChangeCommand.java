package com.veloservice.ordenes.application.dto;

import com.veloservice.config.enums.EstadoOrdenEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Application command for changing a work order state.
 */
@Data
@AllArgsConstructor
public class OrdenEstadoChangeCommand {
    private EstadoOrdenEnum nuevoEstado;
    private String observacion;
}
