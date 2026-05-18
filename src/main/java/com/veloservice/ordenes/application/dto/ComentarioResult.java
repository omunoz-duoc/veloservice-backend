package com.veloservice.ordenes.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class ComentarioResult {
    private UUID id;
    private String autor;
    private String texto;
    private OffsetDateTime creadoEn;
}
