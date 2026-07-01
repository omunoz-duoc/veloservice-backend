package com.veloservice.ordenes.interfaces.rest.dto;

public record MultimediaPresignResponse(
        String presignedUrl,
        String objectKey,
        String publicUrl
) {
}
