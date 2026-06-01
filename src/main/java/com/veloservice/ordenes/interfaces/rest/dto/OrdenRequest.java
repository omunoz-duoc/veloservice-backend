package com.veloservice.ordenes.interfaces.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import com.veloservice.ordenes.domain.TipoOrdenEnum;

import java.util.List;
import java.util.UUID;

/**
 * Work order creation request payload.
 */
@Data
public class OrdenRequest {
    @NotNull
    private UUID bicicletaId;
    @NotNull
    private TipoOrdenEnum tipo;
    private String diagnosticoInicial;
    private String observacionesCliente;
}