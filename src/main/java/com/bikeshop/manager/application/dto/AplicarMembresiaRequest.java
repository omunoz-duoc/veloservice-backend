package com.bikeshop.manager.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * Request payload to apply a membership to a work order.
 */
@Data
public class AplicarMembresiaRequest {
    @NotNull
    private UUID membresiaId;
}
