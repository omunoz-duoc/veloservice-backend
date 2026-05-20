package com.veloservice.ordenes.interfaces.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresignResponse {
    private final String uploadUrl;
    private final String fileKey;
}
