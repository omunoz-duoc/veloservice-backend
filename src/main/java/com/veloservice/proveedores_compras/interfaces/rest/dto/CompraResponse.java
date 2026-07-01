package com.veloservice.proveedores_compras.interfaces.rest.dto;

import com.veloservice.proveedores_compras.domain.EstadoCompraEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Purchase response payload.
 */
@Data
@Builder
@AllArgsConstructor
public class CompraResponse {
    private UUID id;
    private String proveedorNombre;
    private String numeroFactura;
    private BigDecimal neto;
    private BigDecimal iva;
    private BigDecimal total;
    private EstadoCompraEnum estado;
    private LocalDate fechaCompra;
    private List<CompraLineaResponse> lineas;
}
