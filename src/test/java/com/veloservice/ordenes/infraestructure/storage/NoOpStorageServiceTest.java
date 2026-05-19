package com.veloservice.ordenes.infraestructure.storage;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoOpStorageServiceTest {

    private final NoOpStorageService service = new NoOpStorageService();

    @Test
    void presignReturnsNonNullUrlContainingFileKey() {
        String url = service.presign("ordenes/abc/photo.jpg", "image/jpeg", 10);
        assertThat(url).isNotBlank();
        assertThat(url).contains("ordenes/abc/photo.jpg");
    }

    @Test
    void publicUrlReturnsUrlContainingFileKey() {
        String url = service.publicUrl("ordenes/abc/photo.jpg");
        assertThat(url).isNotBlank();
        assertThat(url).contains("ordenes/abc/photo.jpg");
    }
}
