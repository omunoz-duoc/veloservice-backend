package com.veloservice.ordenes.infraestructure.storage;

import com.veloservice.config.storage.R2Properties;
import com.veloservice.ordenes.application.port.R2StoragePort;
import com.veloservice.shared.application.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class R2MediaStorage implements R2StoragePort {

    private final S3Client r2Client;
    private final S3Presigner r2Presigner;
    private final R2Properties properties;

    @Override
    public PresignedUpload presignPut(String objectKey, String contentType, Duration expiry) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(properties.bucketName())
                    .key(objectKey)
                    .contentType(contentType)
                    .build();
            PutObjectPresignRequest request = PutObjectPresignRequest.builder()
                    .signatureDuration(expiry)
                    .putObjectRequest(putRequest)
                    .build();
            return new PresignedUpload(r2Presigner.presignPutObject(request).url().toString());
        } catch (SdkException ex) {
            throw new ServiceUnavailableException("Almacenamiento R2 no disponible", ex);
        }
    }

    @Override
    public Optional<ObjectMetadata> head(String objectKey) {
        try {
            HeadObjectResponse response = r2Client.headObject(HeadObjectRequest.builder()
                    .bucket(properties.bucketName())
                    .key(objectKey)
                    .build());
            return Optional.of(new ObjectMetadata(response.contentType(), response.contentLength()));
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                return Optional.empty();
            }
            throw new ServiceUnavailableException("Almacenamiento R2 no disponible", ex);
        } catch (SdkException ex) {
            throw new ServiceUnavailableException("Almacenamiento R2 no disponible", ex);
        }
    }
}
