package com.veloservice.ordenes.application.port;

import java.time.Duration;
import java.util.Optional;

public interface R2StoragePort {

    PresignedUpload presignPut(String objectKey, String contentType, Duration expiry);

    Optional<ObjectMetadata> head(String objectKey);

    record PresignedUpload(String url) {
    }

    record ObjectMetadata(String contentType, long contentLength) {
    }
}
