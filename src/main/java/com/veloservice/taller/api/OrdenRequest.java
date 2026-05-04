package com.veloservice.taller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import com.veloservice.config.enums.TipoOrdenEnum;

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
    @NotEmpty(message = "Debe adjuntar al menos una evidencia multimedia (RN01)")
    @Valid
    private List<MultimediaRequest> multimedia;
}