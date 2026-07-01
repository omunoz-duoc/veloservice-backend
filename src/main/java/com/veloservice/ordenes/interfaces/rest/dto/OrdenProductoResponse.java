package com.veloservice.ordenes.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class OrdenProductoResponse {
    private UUID id;
    @JsonProperty("productoId")
    private UUID productoId;
    private String nombre;
    private String sku;
    private Integer cantidad;
    @JsonProperty("precioVenta")
    private BigDecimal precioVenta;
    private BigDecimal precioAplicado;
    private String notas;
    private Boolean proporcionadoPorCliente;
    private UUID usuarioId;
    private String usuario;
    private OffsetDateTime createdAt;
}
