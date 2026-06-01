package com.veloservice.ordenes.application.dto;

import java.time.OffsetDateTime;

public record MultimediaResult(
    String usuario,
    String tipoArchivo,
    String url,
    String etapa,
    String descripcion,
    OffsetDateTime createdAt
) {}
