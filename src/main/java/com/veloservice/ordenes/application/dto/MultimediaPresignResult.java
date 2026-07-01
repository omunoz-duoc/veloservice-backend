package com.veloservice.ordenes.application.dto;

public record MultimediaPresignResult(
        String presignedUrl,
        String objectKey,
        String publicUrl
) {
}
