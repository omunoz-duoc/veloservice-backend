package com.veloservice.config.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;

@ConfigurationProperties(prefix = "cloudflare.r2")
@Validated
public record R2Properties(
        @NotBlank String accountId,
        @NotBlank String accessKeyId,
        @NotBlank String secretAccessKey,
        @NotBlank String bucketName,
        @NotBlank String publicBaseUrl,
        @NotNull Duration presignExpiry
) {
}
