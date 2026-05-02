package com.bikeshop.manager.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * Work order creation request payload.
 */
@Data
public class OrdenRequest {
    @NotNull
    private UUID bicicletaId;
    @NotBlank
    private String tipo;
    private String diagnosticoInicial;
    private String observacionesCliente;
    @NotEmpty(message = "Debe adjuntar al menos una evidencia multimedia (RN01)")
    @Valid
    private List<MultimediaRequest> multimedia;
}
