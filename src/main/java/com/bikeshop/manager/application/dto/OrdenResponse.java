package com.bikeshop.manager.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
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
    private String estado;
    private String tipo;
    private UUID bicicletaId;
    private UUID mecanicoId;
    private String diagnosticoInicial;
    private LocalDateTime fechaIngreso;
    private LocalDateTime fechaPrometida;
}
