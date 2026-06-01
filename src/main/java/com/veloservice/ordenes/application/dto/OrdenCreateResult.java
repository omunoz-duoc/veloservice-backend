package com.veloservice.ordenes.application.dto;

import java.util.UUID;

public record OrdenCreateResult(
    UUID id, 
    String numeroOrden
) {}
