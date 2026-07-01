package com.veloservice.ordenes.application.port;

import java.io.IOException;
import java.util.UUID;

public interface MediaStoragePort {
    StoredMedia store(UUID tallerId, UUID ordenId, String contentType, byte[] content) throws IOException;

    void delete(String key);

    record StoredMedia(String key, String url) {
    }
}
