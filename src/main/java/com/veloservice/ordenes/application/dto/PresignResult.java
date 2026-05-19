package com.veloservice.ordenes.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresignResult {
    private final String uploadUrl;
    private final String fileKey;
}
