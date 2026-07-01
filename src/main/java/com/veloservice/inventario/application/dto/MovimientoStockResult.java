package com.veloservice.inventario.application.dto;

import com.veloservice.inventario.domain.TipoMovimientoEnum;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MovimientoStockResult {
    private UUID id;
    private UUID productoId;
    private TipoMovimientoEnum tipo;
    private Integer cantidad;
    private Integer stockAnterior;
    private Integer stockPosterior;
    private String motivo;
    private OffsetDateTime createdAt;
}
