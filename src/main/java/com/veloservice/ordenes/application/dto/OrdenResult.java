package com.veloservice.ordenes.application.dto;

import com.veloservice.config.enums.EstadoOrdenEnum;
import com.veloservice.config.enums.PrioridadOrdenEnum;
import com.veloservice.config.enums.TipoOrdenEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Application result for work order queries.
 */
@Data
@Builder
@AllArgsConstructor
public class OrdenResult {
    private UUID id;
    private String numeroOrden;
    private EstadoOrdenEnum estado;
    private TipoOrdenEnum tipo;
    private PrioridadOrdenEnum prioridad;
    private UUID bicicletaId;
    private UUID mecanicoId;
    private UUID mecanicoAsignadoId;
    private String descripcionTrabajo;
    private String notasInternas;
    private String diagnosticoInicial;
    private OffsetDateTime fechaIngreso;
    private OffsetDateTime fechaPrometida;
    private LocalDate fechaEstimadaEntrega;
}
