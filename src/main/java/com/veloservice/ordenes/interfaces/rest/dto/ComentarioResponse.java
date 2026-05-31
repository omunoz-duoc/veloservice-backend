package com.veloservice.ordenes.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class ComentarioResponse {
    private UUID id;
    private String autor;
    private String texto;
    @JsonProperty("creadoEn")
    private OffsetDateTime creadoEn;
}
