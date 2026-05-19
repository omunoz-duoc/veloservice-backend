package com.veloservice.ordenes.application.usecase;

public interface StorageService {
    String presign(String fileKey, String contentType, int expiryMinutes);
    String publicUrl(String fileKey);
}
