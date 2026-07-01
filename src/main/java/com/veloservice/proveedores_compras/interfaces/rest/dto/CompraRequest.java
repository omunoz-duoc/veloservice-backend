package com.veloservice.proveedores_compras.interfaces.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Purchase request payload.
 */
@Data
public class CompraRequest {
    @NotNull
    private UUID sucursalProveedorId;
    private String numeroFactura;
    @NotNull
    private LocalDate fechaCompra;
    private String notas;
    @NotEmpty
    @Valid
    private List<CompraLineaRequest> lineas;
}