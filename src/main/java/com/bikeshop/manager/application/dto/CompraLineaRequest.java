package com.bikeshop.manager.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Purchase line request payload.
 */
@Data
public class CompraLineaRequest {
    @NotNull
    private UUID productoId;
    @NotNull
    @Min(1)
    private Integer cantidad;
    @NotNull
    @Min(0)
    private BigDecimal precioUnitario;
}
