package com.veloservice.clientes.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Full customer detail response payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDetalleResponse {
    private String nombre;
    private String email;
    private String telefono;
    private String direccion;
    private String rut;
    private OffsetDateTime clienteDesde;
    private int bicicletasCount;
    private List<BicicletaDetalleItem> bicicletas;
    private long otsCount;
    private List<OrdenResumenItem> lastOts;
}
