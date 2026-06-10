package com.veloservice.ordenes.application.dto;

public record MultimediaConfirmationResult(
        MultimediaResult multimedia,
        boolean created
) {
}
