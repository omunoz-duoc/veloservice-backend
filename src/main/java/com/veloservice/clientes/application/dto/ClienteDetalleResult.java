package com.veloservice.clientes.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application result for the full customer detail query.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDetalleResult {
    private UUID id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String direccion;
    private String rut;
    private OffsetDateTime clienteDesde;
    private int bicicletasCount;
    private List<BicicletaDetalleResult> bicicletas;
    private long otsCount;
    private List<OrdenResumenResult> lastOts;
}
