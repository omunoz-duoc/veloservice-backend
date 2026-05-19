package com.veloservice.ordenes.infraestructure.storage;

import com.veloservice.ordenes.application.usecase.StorageService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class NoOpStorageService implements StorageService {

    @Override
    public String presign(String fileKey, String contentType, int expiryMinutes) {
        return "http://localhost/no-op-presign/" + fileKey;
    }

    @Override
    public String publicUrl(String fileKey) {
        return "http://localhost/media/" + fileKey;
    }
}
