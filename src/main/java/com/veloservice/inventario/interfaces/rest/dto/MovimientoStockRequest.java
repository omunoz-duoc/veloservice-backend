package com.veloservice.inventario.interfaces.rest.dto;

import com.veloservice.inventario.domain.TipoMovimientoEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class MovimientoStockRequest {
    @NotNull
    private TipoMovimientoEnum tipo;

    @NotNull
    @Min(1)
    private Integer cantidad;

    private String motivo;
    private UUID sucursalId;
}
