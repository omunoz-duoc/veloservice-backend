package com.veloservice.ordenes.application.dto;

import com.veloservice.config.enums.EstadoOrdenEnum;
import com.veloservice.config.enums.TipoOrdenEnum;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.OffsetDateTime;

/**
 * Lightweight application result for work order listings.
 */
@Value
@AllArgsConstructor
public class OrdenResumenResult {
    String externalId;
    TipoOrdenEnum tipo;
    OffsetDateTime fechaIngreso;
    String nombreMecanico;
    String nombreCliente;
    String descripcion;
    String observacionesCliente;
    String bicicletaMarca;
    String bicicletaColor;
    String bicicletaTipo;
    String bicicletaTalla;
    EstadoOrdenEnum estado;
}