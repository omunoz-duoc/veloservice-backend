# Photo Upload via Cloudflare R2 — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Wire Cloudflare R2 presigned upload flow into the existing `multimedia` layer so photos can be attached to service orders from the Expo app.

**Architecture:** Two new endpoints on `OrdenController` — `/presign` (returns a signed R2 PUT URL) and `/confirm` (saves the resulting public URL to the `multimedia` table). The Expo app uploads the file directly to R2 without routing bytes through Spring. A `StorageService` interface isolates R2 from the domain layer; a `NoOpStorageService` activates under the `dev` profile so tests never touch real R2.

**Tech Stack:** Spring Boot 3.3 / Java 21 / AWS SDK v2 S3 (S3-compatible with R2) / Cloudflare R2 / Expo + expo-image-picker

**Spec:** `docs/superpowers/specs/2026-05-19-photo-upload-r2-design.md`

---

## File Map

### Create
| File | Responsibility |
|------|---------------|
| `src/main/java/com/veloservice/config/storage/R2Properties.java` | `@ConfigurationProperties("r2")` record — holds R2 credentials and config |
| `src/main/java/com/veloservice/config/storage/R2Config.java` | `@Profile("!dev")` — builds `S3Presigner` bean pointing at R2 endpoint |
| `src/main/java/com/veloservice/ordenes/application/usecase/StorageService.java` | Interface: `presign()` + `publicUrl()` — keeps R2 out of domain logic |
| `src/main/java/com/veloservice/ordenes/infraestructure/storage/R2StorageService.java` | `@Profile("!dev")` implementation using `S3Presigner` |
| `src/main/java/com/veloservice/ordenes/infraestructure/storage/NoOpStorageService.java` | `@Profile("dev")` no-op for tests and local dev |
| `src/main/java/com/veloservice/ordenes/application/dto/PresignResult.java` | Application-layer DTO returned by `MultimediaService.generarPresign()` |
| `src/main/java/com/veloservice/ordenes/interfaces/rest/PresignRequest.java` | REST request body for `/presign` |
| `src/main/java/com/veloservice/ordenes/interfaces/rest/PresignResponse.java` | REST response body for `/presign` |
| `src/main/java/com/veloservice/ordenes/interfaces/rest/ConfirmRequest.java` | REST request body for `/confirm` |
| `src/test/java/com/veloservice/ordenes/infraestructure/storage/R2StorageServiceTest.java` | Unit test for `R2StorageService` |
| `src/test/java/com/veloservice/ordenes/application/usecase/MultimediaServicePresignTest.java` | Unit tests for `generarPresign()` and `confirmar()` |
| `src/test/java/com/veloservice/ordenes/interfaces/rest/MultimediaPresignControllerTest.java` | `@WebMvcTest` tests for the two new controller endpoints |

### Modify
| File | What changes |
|------|-------------|
| `pom.xml` | Add `software.amazon.awssdk:s3` + `url-connection-client` dependencies |
| `src/main/resources/application-prod.yml` | Add `r2:` config block with env var references |
| `src/main/java/com/veloservice/ordenes/application/usecase/MultimediaService.java` | Inject `StorageService`; add `generarPresign()` + `confirmar()` |
| `src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java` | Add `/presign` and `/confirm` endpoints |

---

## Task 1: Add AWS SDK v2 S3 Dependency

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: Add dependencies after the opencsv block**

In `pom.xml`, find this block (around line 37–41):
```xml
        <dependency>
    <groupId>com.opencsv</groupId>
    <artifactId>opencsv</artifactId>
    <version>5.9</version>
</dependency>
```

Replace with:
```xml
        <dependency>
    <groupId>com.opencsv</groupId>
    <artifactId>opencsv</artifactId>
    <version>5.9</version>
</dependency>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>s3</artifactId>
            <version>2.26.31</version>
        </dependency>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>url-connection-client</artifactId>
            <version>2.26.31</version>
        </dependency>
```

- [ ] **Step 2: Verify dependency resolves**

```bash
./mvnw dependency:resolve -q
```
Expected: BUILD SUCCESS with no errors.

- [ ] **Step 3: Commit**

```bash
git add pom.xml
git commit -m "build: add AWS SDK v2 S3 for Cloudflare R2 presign"
```

---

## Task 2: R2 Configuration Layer

**Files:**
- Create: `src/main/java/com/veloservice/config/storage/R2Properties.java`
- Create: `src/main/java/com/veloservice/config/storage/R2Config.java`
- Modify: `src/main/resources/application-prod.yml`

- [ ] **Step 1: Create `R2Properties.java`**

```java
package com.veloservice.config.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "r2")
public record R2Properties(
        String accountId,
        String accessKey,
        String secretKey,
        String bucket,
        String publicDomain,
        int presignExpiryMinutes
) {}
```

- [ ] **Step 2: Create `R2Config.java`**

```java
package com.veloservice.config.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@Profile("!dev")
@EnableConfigurationProperties(R2Properties.class)
public class R2Config {

    @Bean
    public S3Presigner s3Presigner(R2Properties props) {
        return S3Presigner.builder()
                .endpointOverride(URI.create(
                        "https://" + props.accountId() + ".r2.cloudflarestorage.com"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.accessKey(), props.secretKey())))
                .region(Region.of("auto"))
                .build();
    }
}
```

- [ ] **Step 3: Add r2 block to `application-prod.yml`**

Append to `src/main/resources/application-prod.yml`:
```yaml
r2:
  account-id: ${R2_ACCOUNT_ID}
  access-key: ${R2_ACCESS_KEY}
  secret-key: ${R2_SECRET_KEY}
  bucket: ${R2_BUCKET}
  public-domain: ${R2_PUBLIC_DOMAIN}
  presign-expiry-minutes: 10
```

- [ ] **Step 4: Verify compilation**

```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/veloservice/config/storage/ src/main/resources/application-prod.yml
git commit -m "feat: add R2 configuration properties and S3Presigner bean"
```

---

## Task 3: StorageService Interface + NoOpStorageService

**Files:**
- Create: `src/main/java/com/veloservice/ordenes/application/usecase/StorageService.java`
- Create: `src/main/java/com/veloservice/ordenes/infraestructure/storage/NoOpStorageService.java`
- Create: `src/test/java/com/veloservice/ordenes/infraestructure/storage/NoOpStorageServiceTest.java`

- [ ] **Step 1: Write the failing test**

Create `src/test/java/com/veloservice/ordenes/infraestructure/storage/NoOpStorageServiceTest.java`:
```java
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
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./mvnw test -Dtest=NoOpStorageServiceTest -pl . -q 2>&1 | tail -20
```
Expected: FAIL — `NoOpStorageService` does not exist yet.

- [ ] **Step 3: Create `StorageService.java` interface**

```java
package com.veloservice.ordenes.application.usecase;

public interface StorageService {
    String presign(String fileKey, String contentType, int expiryMinutes);
    String publicUrl(String fileKey);
}
```

- [ ] **Step 4: Create `NoOpStorageService.java`**

```java
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
```

- [ ] **Step 5: Run test to verify it passes**

```bash
./mvnw test -Dtest=NoOpStorageServiceTest -pl . -q 2>&1 | tail -10
```
Expected: BUILD SUCCESS, tests: 2 passed.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/application/usecase/StorageService.java \
        src/main/java/com/veloservice/ordenes/infraestructure/storage/NoOpStorageService.java \
        src/test/java/com/veloservice/ordenes/infraestructure/storage/NoOpStorageServiceTest.java
git commit -m "feat: add StorageService interface and NoOpStorageService for dev profile"
```

---

## Task 4: R2StorageService

**Files:**
- Create: `src/main/java/com/veloservice/ordenes/infraestructure/storage/R2StorageService.java`
- Create: `src/test/java/com/veloservice/ordenes/infraestructure/storage/R2StorageServiceTest.java`

- [ ] **Step 1: Write the failing test**

Create `src/test/java/com/veloservice/ordenes/infraestructure/storage/R2StorageServiceTest.java`:
```java
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
                "my-bucket", "media.example.com", 10);
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
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./mvnw test -Dtest=R2StorageServiceTest -pl . -q 2>&1 | tail -20
```
Expected: FAIL — `R2StorageService` does not exist yet.

- [ ] **Step 3: Create `R2StorageService.java`**

```java
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
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./mvnw test -Dtest=R2StorageServiceTest -pl . -q 2>&1 | tail -10
```
Expected: BUILD SUCCESS, tests: 3 passed.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/infraestructure/storage/R2StorageService.java \
        src/test/java/com/veloservice/ordenes/infraestructure/storage/R2StorageServiceTest.java
git commit -m "feat: implement R2StorageService using S3Presigner"
```

---

## Task 5: PresignResult DTO + MultimediaService.generarPresign()

**Files:**
- Create: `src/main/java/com/veloservice/ordenes/application/dto/PresignResult.java`
- Create: `src/test/java/com/veloservice/ordenes/application/usecase/MultimediaServicePresignTest.java`
- Modify: `src/main/java/com/veloservice/ordenes/application/usecase/MultimediaService.java`

- [ ] **Step 1: Create `PresignResult.java`**

```java
package com.veloservice.ordenes.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresignResult {
    private final String uploadUrl;
    private final String fileKey;
}
```

- [ ] **Step 2: Write the failing tests**

Create `src/test/java/com/veloservice/ordenes/application/usecase/MultimediaServicePresignTest.java`:
```java
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
    void generarPresignUsespngExtensionForPngContentType() {
        UUID ordenId = UUID.randomUUID();
        when(storageService.presign(anyString(), eq("image/png"), anyInt()))
                .thenReturn("https://r2.example.com/upload?sig=png");

        PresignResult result = service.generarPresign(ordenId, "foto.png", "image/png", 512L);

        assertThat(result.getFileKey()).endsWith(".png");
    }
}
```

- [ ] **Step 3: Run tests to verify they fail**

```bash
./mvnw test -Dtest=MultimediaServicePresignTest -pl . -q 2>&1 | tail -20
```
Expected: FAIL — `generarPresign` method does not exist.

- [ ] **Step 4: Add `StorageService` field and `generarPresign()` to `MultimediaService`**

In `MultimediaService.java`, add the import and field after existing imports/fields:

Add imports (after existing imports):
```java
import com.veloservice.ordenes.application.dto.PresignResult;
import java.util.List;
```

Add field after `private final MultimediaRepository multimediaRepository;`:
```java
    private final StorageService storageService;
```

Add method after the existing `eliminar()` method:
```java
    public PresignResult generarPresign(UUID ordenId, String fileName, String contentType, long fileSize) {
        if (!List.of("image/jpeg", "image/png").contains(contentType)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Use image/jpeg o image/png");
        }
        if (fileSize > 10_485_760L) {
            throw new IllegalArgumentException("El archivo supera el límite de 10 MB");
        }
        String ext = "image/jpeg".equals(contentType) ? "jpg" : "png";
        String fileKey = "ordenes/" + ordenId + "/" + UUID.randomUUID() + "." + ext;
        String uploadUrl = storageService.presign(fileKey, contentType, 10);
        return new PresignResult(uploadUrl, fileKey);
    }
```

- [ ] **Step 5: Run tests to verify they pass**

```bash
./mvnw test -Dtest=MultimediaServicePresignTest -pl . -q 2>&1 | tail -10
```
Expected: BUILD SUCCESS, tests: 4 passed.

- [ ] **Step 6: Run full test suite to catch regressions**

```bash
./mvnw test -pl . -q 2>&1 | tail -15
```
Expected: BUILD SUCCESS, all previously passing tests still pass.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/application/dto/PresignResult.java \
        src/main/java/com/veloservice/ordenes/application/usecase/MultimediaService.java \
        src/test/java/com/veloservice/ordenes/application/usecase/MultimediaServicePresignTest.java
git commit -m "feat: add generarPresign() to MultimediaService"
```

---

## Task 6: MultimediaService.confirmar()

**Files:**
- Modify: `src/main/java/com/veloservice/ordenes/application/usecase/MultimediaService.java`
- Modify: `src/test/java/com/veloservice/ordenes/application/usecase/MultimediaServicePresignTest.java`

- [ ] **Step 1: Write the failing test**

Add this test to `MultimediaServicePresignTest.java` (inside the class, after existing tests):
```java
    @Test
    void confirmarBuildsPublicUrlAndSavesMultimedia() {
        UUID ordenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String fileKey = "ordenes/" + ordenId + "/abc.jpg";
        String publicUrl = "https://media.example.com/" + fileKey;

        com.veloservice.ordenes.domain.model.Multimedia saved =
                com.veloservice.ordenes.domain.model.Multimedia.builder()
                        .id(UUID.randomUUID())
                        .ordenId(ordenId)
                        .usuarioId(userId)
                        .url(publicUrl)
                        .tipoArchivo(com.veloservice.config.enums.TipoArchivoEnum.imagen)
                        .etapa(com.veloservice.config.enums.EtapaMultimediaEnum.ingreso)
                        .createdAt(java.time.OffsetDateTime.now())
                        .build();

        when(storageService.publicUrl(fileKey)).thenReturn(publicUrl);
        when(multimediaRepository.save(any())).thenReturn(saved);

        try (org.mockito.MockedStatic<com.veloservice.config.security.UsuarioContext> ctx =
                     org.mockito.Mockito.mockStatic(com.veloservice.config.security.UsuarioContext.class)) {
            ctx.when(com.veloservice.config.security.UsuarioContext::getCurrentUser).thenReturn(userId);

            com.veloservice.ordenes.application.dto.MultimediaResult result =
                    service.confirmar(ordenId, "ingreso", fileKey,
                            com.veloservice.config.enums.TipoArchivoEnum.imagen, null);

            assertThat(result.getUrl()).isEqualTo(publicUrl);
        }
    }
```

Also add `import static org.mockito.ArgumentMatchers.any;` to the import block.

- [ ] **Step 2: Run test to verify it fails**

```bash
./mvnw test -Dtest=MultimediaServicePresignTest#confirmarBuildsPublicUrlAndSavesMultimedia -pl . -q 2>&1 | tail -20
```
Expected: FAIL — `confirmar` method does not exist.

- [ ] **Step 3: Add `confirmar()` to `MultimediaService`**

Add the method after `generarPresign()`:
```java
    public MultimediaResult confirmar(UUID ordenId, String etapa, String fileKey,
                                      TipoArchivoEnum tipoArchivo, String descripcion) {
        String publicUrl = storageService.publicUrl(fileKey);
        return subir(ordenId, etapa, new MultimediaCreateCommand(publicUrl, tipoArchivo, descripcion));
    }
```

Add `import com.veloservice.config.enums.TipoArchivoEnum;` to imports.

- [ ] **Step 4: Run tests to verify they pass**

```bash
./mvnw test -Dtest=MultimediaServicePresignTest -pl . -q 2>&1 | tail -10
```
Expected: BUILD SUCCESS, tests: 5 passed.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/application/usecase/MultimediaService.java \
        src/test/java/com/veloservice/ordenes/application/usecase/MultimediaServicePresignTest.java
git commit -m "feat: add confirmar() to MultimediaService"
```

---

## Task 7: REST DTOs + Controller Endpoints

**Files:**
- Create: `src/main/java/com/veloservice/ordenes/interfaces/rest/PresignRequest.java`
- Create: `src/main/java/com/veloservice/ordenes/interfaces/rest/PresignResponse.java`
- Create: `src/main/java/com/veloservice/ordenes/interfaces/rest/ConfirmRequest.java`
- Create: `src/test/java/com/veloservice/ordenes/interfaces/rest/MultimediaPresignControllerTest.java`
- Modify: `src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java`

- [ ] **Step 1: Write the failing tests**

Create `src/test/java/com/veloservice/ordenes/interfaces/rest/MultimediaPresignControllerTest.java`:
```java
package com.veloservice.ordenes.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.enums.EtapaMultimediaEnum;
import com.veloservice.config.enums.TipoArchivoEnum;
import com.veloservice.config.security.JwtTokenProvider;
import com.veloservice.ordenes.application.dto.MultimediaResult;
import com.veloservice.ordenes.application.dto.PresignResult;
import com.veloservice.ordenes.application.usecase.ComentarioService;
import com.veloservice.ordenes.application.usecase.MultimediaService;
import com.veloservice.ordenes.application.usecase.OrdenService;
import com.veloservice.ordenes.infraestructure.persistence.repository.MultimediaRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenProductoRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdenController.class)
@AutoConfigureMockMvc(addFilters = false)
class MultimediaPresignControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private OrdenService ordenService;
    @MockBean private OrdenRepository ordenRepository;
    @MockBean private OrdenProductoRepository ordenProductoRepository;
    @MockBean private MultimediaService multimediaService;
    @MockBean private MultimediaRepository multimediaRepository;
    @MockBean private ComentarioService comentarioService;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private UsuarioRepository usuarioRepository;

    @Test
    void presignReturnsUploadUrlAndFileKey() throws Exception {
        UUID ordenId = UUID.randomUUID();
        String fileKey = "ordenes/" + ordenId + "/uuid.jpg";
        when(ordenService.resolveOrdenId(ordenId.toString())).thenReturn(ordenId);
        when(multimediaService.generarPresign(
                eq(ordenId), eq("foto.jpg"), eq("image/jpeg"), eq(1024L)))
                .thenReturn(new PresignResult("https://r2.example.com/upload?sig=abc", fileKey));

        mockMvc.perform(post("/ordenes/{id}/multimedia/presign", ordenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fileName", "foto.jpg",
                                "contentType", "image/jpeg",
                                "fileSize", 1024
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadUrl").value("https://r2.example.com/upload?sig=abc"))
                .andExpect(jsonPath("$.fileKey").value(fileKey));
    }

    @Test
    void confirmSavesMultimediaAndReturnsResponse() throws Exception {
        UUID ordenId = UUID.randomUUID();
        String fileKey = "ordenes/" + ordenId + "/uuid.jpg";
        String publicUrl = "https://media.example.com/" + fileKey;

        when(ordenService.resolveOrdenId(ordenId.toString())).thenReturn(ordenId);
        MultimediaResult result = MultimediaResult.builder()
                .id(UUID.randomUUID())
                .ordenId(ordenId)
                .usuarioId(UUID.randomUUID())
                .url(publicUrl)
                .tipoArchivo(TipoArchivoEnum.imagen)
                .etapa(EtapaMultimediaEnum.ingreso)
                .createdAt(OffsetDateTime.now())
                .build();
        when(multimediaService.confirmar(
                eq(ordenId), eq("ingreso"), eq(fileKey),
                eq(TipoArchivoEnum.imagen), isNull()))
                .thenReturn(result);

        mockMvc.perform(post("/ordenes/{id}/multimedia/confirm", ordenId)
                        .param("etapa", "ingreso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fileKey", fileKey,
                                "tipoArchivo", "imagen"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(publicUrl));
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

```bash
./mvnw test -Dtest=MultimediaPresignControllerTest -pl . -q 2>&1 | tail -20
```
Expected: FAIL — new endpoints don't exist yet.

- [ ] **Step 3: Create `PresignRequest.java`**

```java
package com.veloservice.ordenes.interfaces.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PresignRequest {
    @NotBlank
    private String fileName;
    @NotBlank
    private String contentType;
    @NotNull
    @Positive
    private Long fileSize;
}
```

- [ ] **Step 4: Create `PresignResponse.java`**

```java
package com.veloservice.ordenes.interfaces.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresignResponse {
    private final String uploadUrl;
    private final String fileKey;
}
```

- [ ] **Step 5: Create `ConfirmRequest.java`**

```java
package com.veloservice.ordenes.interfaces.rest;

import com.veloservice.config.enums.TipoArchivoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConfirmRequest {
    @NotBlank
    private String fileKey;
    @NotNull
    private TipoArchivoEnum tipoArchivo;
    private String descripcion;
}
```

- [ ] **Step 6: Add imports to `OrdenController.java`**

In `OrdenController.java`, add to the import block after the existing multimedia imports:
```java
import com.veloservice.ordenes.application.dto.PresignResult;
```

- [ ] **Step 7: Add two endpoints to `OrdenController.java`**

In `OrdenController.java`, insert after the `eliminarMultimedia` method (around line 301) and before `listarProductos`:
```java
    /**
     * Generates a presigned R2 PUT URL for direct photo upload.
     */
    @PostMapping("/{id}/multimedia/presign")
    public ResponseEntity<PresignResponse> presignMultimedia(
            @PathVariable String id,
            @Valid @RequestBody PresignRequest request) {
        UUID ordenId = ordenService.resolveOrdenId(id);
        PresignResult result = multimediaService.generarPresign(
                ordenId, request.getFileName(), request.getContentType(), request.getFileSize());
        return ResponseEntity.ok(new PresignResponse(result.getUploadUrl(), result.getFileKey()));
    }

    /**
     * Confirms an upload by saving the R2 object URL to the multimedia table.
     */
    @PostMapping("/{id}/multimedia/confirm")
    public ResponseEntity<MultimediaResponse> confirmMultimedia(
            @PathVariable String id,
            @RequestParam String etapa,
            @Valid @RequestBody ConfirmRequest request) {
        UUID ordenId = ordenService.resolveOrdenId(id);
        var result = multimediaService.confirmar(
                ordenId, etapa, request.getFileKey(),
                request.getTipoArchivo(), request.getDescripcion());
        return ResponseEntity.ok(MultimediaResponse.builder()
                .id(result.getId())
                .ordenId(result.getOrdenId())
                .usuarioId(result.getUsuarioId())
                .url(result.getUrl())
                .tipoArchivo(result.getTipoArchivo())
                .etapa(result.getEtapa())
                .descripcion(result.getDescripcion())
                .createdAt(result.getCreatedAt())
                .build());
    }
```

Also add import for the new request/response types (they're in the same package so no import needed, but add `@Valid` import if not present — it's already imported from the existing endpoints).

- [ ] **Step 8: Run tests to verify they pass**

```bash
./mvnw test -Dtest=MultimediaPresignControllerTest -pl . -q 2>&1 | tail -10
```
Expected: BUILD SUCCESS, tests: 2 passed.

- [ ] **Step 9: Run full test suite**

```bash
./mvnw test -pl . -q 2>&1 | tail -15
```
Expected: BUILD SUCCESS, all tests pass.

- [ ] **Step 10: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/interfaces/rest/PresignRequest.java \
        src/main/java/com/veloservice/ordenes/interfaces/rest/PresignResponse.java \
        src/main/java/com/veloservice/ordenes/interfaces/rest/ConfirmRequest.java \
        src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java \
        src/test/java/com/veloservice/ordenes/interfaces/rest/MultimediaPresignControllerTest.java
git commit -m "feat: add /presign and /confirm endpoints for R2 photo upload"
```

---

## Cloudflare R2 One-Time Setup (Manual — do before deploying)

These steps happen in the Cloudflare Dashboard, not in code:

1. **Create bucket** — R2 → Create bucket → name: `veloservice-media`
2. **Enable public access** — Bucket → Settings → Public Access → Allow Access → note the `*.r2.dev` URL (or connect custom domain via CNAME) → this is `R2_PUBLIC_DOMAIN`
3. **Create API token** — R2 → Manage R2 API Tokens → Create token → Object Read & Write on `veloservice-media` only → save Account ID, Access Key ID, Secret Access Key
4. **Set bucket CORS** — Bucket → Settings → CORS Policy:
```json
[
  {
    "AllowedOrigins": ["*"],
    "AllowedMethods": ["PUT"],
    "AllowedHeaders": ["Content-Type"],
    "MaxAgeSeconds": 3600
  }
]
```
5. **Add env vars to Cloud Run** — `R2_ACCOUNT_ID`, `R2_ACCESS_KEY`, `R2_SECRET_KEY`, `R2_BUCKET`, `R2_PUBLIC_DOMAIN`

---

## Expo Frontend Steps (separate codebase)

Install dependencies:
```bash
npx expo install expo-image-picker expo-file-system
```

Upload hook (TypeScript):
```typescript
import * as ImagePicker from 'expo-image-picker';
import * as FileSystem from 'expo-file-system';

export async function uploadOrdenPhoto(
  ordenId: string,
  etapa: string,
  apiPost: (url: string, body: unknown) => Promise<unknown>
) {
  // 1. Pick photo
  const result = await ImagePicker.launchImageLibraryAsync({
    mediaTypes: ImagePicker.MediaTypeOptions.Images,
    quality: 0.8,
    allowsEditing: true,
  });
  if (result.canceled) return null;
  const asset = result.assets[0];

  // 2. Get presigned URL from backend
  const { uploadUrl, fileKey } = await apiPost(
    `/ordenes/${ordenId}/multimedia/presign`,
    { fileName: asset.fileName ?? 'photo.jpg', contentType: 'image/jpeg', fileSize: asset.fileSize }
  ) as { uploadUrl: string; fileKey: string };

  // 3. Upload directly to R2 — NO Authorization header
  const fileBase64 = await FileSystem.readAsStringAsync(asset.uri, {
    encoding: FileSystem.EncodingType.Base64,
  });
  const bytes = Uint8Array.from(atob(fileBase64), c => c.charCodeAt(0));
  await fetch(uploadUrl, {
    method: 'PUT',
    headers: { 'Content-Type': 'image/jpeg' },
    body: bytes,
  });

  // 4. Confirm to backend — saves row in multimedia table
  const multimedia = await apiPost(
    `/ordenes/${ordenId}/multimedia/confirm?etapa=${etapa}`,
    { fileKey, tipoArchivo: 'imagen' }
  );

  return multimedia; // { id, url, tipoArchivo, etapa, createdAt, ... }
}
```

**Critical:** `Content-Type` in the `PUT` header must exactly match what was sent to `/presign`. A mismatch returns 403 from R2.

---

## Manual End-to-End Test Checklist

After deploying with R2 env vars set:

- [ ] Call `POST /ordenes/{id}/multimedia/presign` with `{ fileName, contentType, fileSize }` → response contains `uploadUrl` starting with `*.r2.cloudflarestorage.com` and a `fileKey` matching `ordenes/{id}/...`
- [ ] `curl -X PUT "<uploadUrl>" --data-binary @test.jpg -H "Content-Type: image/jpeg"` → HTTP 200
- [ ] Call `POST /ordenes/{id}/multimedia/confirm?etapa=ingreso` with `{ fileKey, tipoArchivo: "imagen" }` → response contains `url` starting with `https://<R2_PUBLIC_DOMAIN>/`
- [ ] Open the returned `url` in a browser → photo renders
- [ ] Call `GET /ordenes/{id}/multimedia` → photo appears in the list
