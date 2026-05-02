package com.bikeshop.manager.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Purchase request payload.
 */
@Data
public class CompraRequest {
    @NotNull
    private UUID proveedorId;
    private String numeroFactura;
    @NotNull
    private LocalDate fechaCompra;
    private String notas;
    @NotEmpty
    @Valid
    private List<CompraLineaRequest> lineas;
}
