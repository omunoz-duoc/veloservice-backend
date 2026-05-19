package com.veloservice.ordenes.infraestructure.storage;

import com.veloservice.config.storage.R2Properties;
import com.veloservice.ordenes.application.usecase.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
@Profile("!dev")
@RequiredArgsConstructor
public class R2StorageService implements StorageService {

    private final S3Presigner s3Presigner;
    private final R2Properties r2Properties;

    @Override
    public String presign(String fileKey, String contentType, int expiryMinutes) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(r2Properties.bucket())
                .key(fileKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expiryMinutes))
                .putObjectRequest(putRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    @Override
    public String publicUrl(String fileKey) {
        return "https://" + r2Properties.publicDomain() + "/" + fileKey;
    }
}
