package com.veloservice.ordenes.application.dto;

import java.time.OffsetDateTime;

public record ComentarioResult(
    String usuario, 
    String texto, 
    OffsetDateTime createdAt
) {}
