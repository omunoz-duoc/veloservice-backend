package com.veloservice.ordenes.application.usecase;

import com.veloservice.ordenes.application.dto.PresignResult;
import com.veloservice.ordenes.infraestructure.persistence.repository.MultimediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultimediaServicePresignTest {

    @Mock private MultimediaRepository multimediaRepository;
    @Mock private StorageService storageService;

    private MultimediaService service;

    @BeforeEach
    void setUp() {
        service = new MultimediaService(multimediaRepository, storageService);
    }

    @Test
    void generarPresignRejectsNonImageContentType() {
        UUID ordenId = UUID.randomUUID();
        assertThatThrownBy(() ->
                service.generarPresign(ordenId, "doc.pdf", "application/pdf", 1024L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("image/jpeg o image/png");
    }

    @Test
    void generarPresignRejectsFileLargerThan10MB() {
        UUID ordenId = UUID.randomUUID();
        assertThatThrownBy(() ->
                service.generarPresign(ordenId, "big.jpg", "image/jpeg", 10_485_761L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("10 MB");
    }

    @Test
    void generarPresignReturnsCorrectFileKeyFormatAndUploadUrl() {
        UUID ordenId = UUID.randomUUID();
        when(storageService.presign(anyString(), eq("image/jpeg"), anyInt()))
                .thenReturn("https://r2.example.com/upload?sig=abc");

        PresignResult result = service.generarPresign(ordenId, "foto.jpg", "image/jpeg", 1024L);

        assertThat(result.getFileKey())
                .matches("ordenes/" + ordenId + "/[0-9a-f\\-]+\\.jpg");
        assertThat(result.getUploadUrl()).isEqualTo("https://r2.example.com/upload?sig=abc");
    }

    @Test
    void generarPresignUsesPngExtensionForPngContentType() {
        UUID ordenId = UUID.randomUUID();
        when(storageService.presign(anyString(), eq("image/png"), anyInt()))
                .thenReturn("https://r2.example.com/upload?sig=png");

        PresignResult result = service.generarPresign(ordenId, "foto.png", "image/png", 512L);

        assertThat(result.getFileKey()).endsWith(".png");
    }
}
