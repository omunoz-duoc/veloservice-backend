package com.veloservice.ordenes.interfaces.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PresignRequest {
    @NotBlank
    private String fileName;
    @NotBlank
    private String contentType;
    @NotNull
    @Positive
    private Long fileSize;
}
