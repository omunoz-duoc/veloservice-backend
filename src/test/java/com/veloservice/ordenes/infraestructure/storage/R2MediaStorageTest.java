package com.veloservice.ordenes.infraestructure.storage;

import com.veloservice.config.storage.R2Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class R2MediaStorageTest {

    @Mock private S3Client s3Client;
    @Mock private S3Presigner presigner;
    @Mock private PresignedPutObjectRequest presignedRequest;

    private R2MediaStorage storage;

    @BeforeEach
    void setUp() {
        storage = new R2MediaStorage(
                s3Client,
                presigner,
                new R2Properties(
                        "account",
                        "access",
                        "secret",
                        "bucket",
                        "https://media.example",
                        Duration.ofMinutes(15)
                )
        );
    }

    @Test
    void presignPutRestrictsBucketKeyContentTypeAndExpiry() throws Exception {
        when(presigner.presignPutObject(org.mockito.ArgumentMatchers.any(PutObjectPresignRequest.class)))
                .thenReturn(presignedRequest);
        when(presignedRequest.url()).thenReturn(new URL("https://r2.example/presigned"));

        var result = storage.presignPut("ordenes/order/file.jpg", "image/jpeg", Duration.ofMinutes(15));

        assertThat(result.url()).isEqualTo("https://r2.example/presigned");
        ArgumentCaptor<PutObjectPresignRequest> captor =
                ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(presigner).presignPutObject(captor.capture());
        assertThat(captor.getValue().signatureDuration()).isEqualTo(Duration.ofMinutes(15));
        assertThat(captor.getValue().putObjectRequest().bucket()).isEqualTo("bucket");
        assertThat(captor.getValue().putObjectRequest().key()).isEqualTo("ordenes/order/file.jpg");
        assertThat(captor.getValue().putObjectRequest().contentType()).isEqualTo("image/jpeg");
    }

    @Test
    void headReturnsStoredMetadata() {
        when(s3Client.headObject(org.mockito.ArgumentMatchers.any(HeadObjectRequest.class)))
                .thenReturn(HeadObjectResponse.builder()
                        .contentType("application/pdf")
                        .contentLength(4096L)
                        .build());

        var result = storage.head("ordenes/order/file.pdf");

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().contentType()).isEqualTo("application/pdf");
        assertThat(result.orElseThrow().contentLength()).isEqualTo(4096L);
        ArgumentCaptor<HeadObjectRequest> captor = ArgumentCaptor.forClass(HeadObjectRequest.class);
        verify(s3Client).headObject(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo("bucket");
        assertThat(captor.getValue().key()).isEqualTo("ordenes/order/file.pdf");
    }
}
