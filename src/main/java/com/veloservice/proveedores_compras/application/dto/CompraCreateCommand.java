package com.veloservice.proveedores_compras.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Application command for creating a purchase.
 */
@Data
@AllArgsConstructor
public class CompraCreateCommand {
    private UUID sucursalProveedorId;
    private String numeroFactura;
    private LocalDate fechaCompra;
    private String notas;
    private List<CompraLineaCommand> lineas;
}
