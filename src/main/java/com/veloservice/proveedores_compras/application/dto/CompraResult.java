package com.veloservice.proveedores_compras.application.dto;

import com.veloservice.config.enums.EstadoCompraEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Application result for purchase queries.
 */
@Data
@Builder
@AllArgsConstructor
public class CompraResult {
    private UUID id;
    private String proveedorNombre;
    private String numeroFactura;
    private BigDecimal neto;
    private BigDecimal iva;
    private BigDecimal total;
    private EstadoCompraEnum estado;
    private LocalDate fechaCompra;
    private List<CompraLineaResult> lineas;
}
