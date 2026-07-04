package com.veloservice.ordenes.infraestructure.storage;

import com.veloservice.config.storage.R2Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class R2StorageServiceTest {

    @Mock
    private S3Presigner s3Presigner;

    private R2StorageService storageService;

    @BeforeEach
    void setUp() {
        R2Properties props = new R2Properties(
                "account123", "accessKey", "secretKey",
                "my-bucket", "https://media.example.com", Duration.ofMinutes(10));
        storageService = new R2StorageService(s3Presigner, props);
    }

    @Test
    void presignReturnsUrlFromPresigner() throws MalformedURLException {
        PresignedPutObjectRequest presigned = mock(PresignedPutObjectRequest.class);
        when(presigned.url()).thenReturn(new URL("https://r2.example.com/upload?sig=abc"));
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presigned);

        String result = storageService.presign("ordenes/abc/photo.jpg", "image/jpeg", 10);

        assertThat(result).isEqualTo("https://r2.example.com/upload?sig=abc");
    }

    @Test
    void presignPassesCorrectBucketAndKeyToPresigner() throws MalformedURLException {
        PresignedPutObjectRequest presigned = mock(PresignedPutObjectRequest.class);
        when(presigned.url()).thenReturn(new URL("https://r2.example.com/upload?sig=abc"));
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presigned);

        storageService.presign("ordenes/abc/photo.jpg", "image/jpeg", 10);

        ArgumentCaptor<PutObjectPresignRequest> captor =
                ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner).presignPutObject(captor.capture());
        assertThat(captor.getValue().putObjectRequest().bucket()).isEqualTo("my-bucket");
        assertThat(captor.getValue().putObjectRequest().key()).isEqualTo("ordenes/abc/photo.jpg");
        assertThat(captor.getValue().putObjectRequest().contentType()).isEqualTo("image/jpeg");
    }

    @Test
    void publicUrlComposesPublicDomainAndFileKey() {
        String url = storageService.publicUrl("ordenes/abc/photo.jpg");
        assertThat(url).isEqualTo("https://media.example.com/ordenes/abc/photo.jpg");
    }
}
