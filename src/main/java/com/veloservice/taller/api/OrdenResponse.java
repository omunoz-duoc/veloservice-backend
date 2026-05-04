package com.veloservice.taller.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import com.veloservice.config.enums.EstadoOrdenEnum;
import com.veloservice.config.enums.TipoOrdenEnum;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Work order response payload.
 */
@Data
@Builder
@AllArgsConstructor
public class OrdenResponse {
    private UUID id;
    private String numeroOrden;
    private EstadoOrdenEnum estado;
    private TipoOrdenEnum tipo;
    private UUID bicicletaId;
    private UUID mecanicoId;
    private String diagnosticoInicial;
    private OffsetDateTime fechaIngreso;
    private OffsetDateTime fechaPrometida;
}