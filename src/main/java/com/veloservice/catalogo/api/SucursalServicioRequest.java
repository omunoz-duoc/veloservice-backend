package com.veloservice.catalogo.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class SucursalServicioRequest {
    @NotNull
    private UUID servicioId;
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal precioPersonalizado;
}