package com.veloservice.ordenes.interfaces.rest.dto;

import java.util.UUID;

public record OrdenCreateResponse(
    UUID id,
    String numeroOrden
) {}
