# Multimedia Upload to Cloudflare R2 — Design Spec

**Date:** 2026-06-09
**Scope:** veloservice-mobile (React Native/Expo) + Spring Boot backend
**Status:** Approved

---

## Context

Users need to attach photos, videos, and files to workshop orders. The mobile app already has a full media capture pipeline (`media.ts`, `MediaSheet`, `useAddMultimedia`) and an existing `upload-client.ts` that proxies multipart files through the backend. The backend multimedia endpoint (`POST /ordenes/{id}/multimedia`) is not yet implemented.

This design replaces the proxy approach with a **presigned URL strategy**: the backend generates a short-lived R2 PUT URL, the mobile app uploads binary directly to Cloudflare R2, then the backend records the metadata.

---

## Architecture

```
Mobile App                     Spring Boot Backend          Cloudflare R2
──────────                     ───────────────────          ─────────────
1. captureMedia()
2. POST /multimedia/presign ─► validate MIME
                               generate object key
                               S3Presigner → R2
                               ◄─ { presignedUrl, objectKey, publicUrl }
3. PUT presignedUrl ────────────────────────────────────► store binary
   (binary, Content-Type)                                  in public bucket
4. POST /multimedia/confirm ─► INSERT multimedia row
                               ◄─ MultimediaResponse
5. invalidate qk.orden(id)
```

---

## Backend: Two New Endpoints

### POST `/ordenes/{id}/multimedia/presign`

**Request body:**
```json
{
  "tipoArchivo": "image/jpeg",
  "nombre": "foto-1.jpg"
}
```

`descripcion` and `etapa` are metadata — they go only in the confirm request, not here.

**Validation:**
- `tipoArchivo` must be in allowlist: `image/jpeg`, `image/png`, `image/webp`, `video/mp4`, `video/quicktime`, `application/pdf`
- Returns `400` with error message if invalid

**Logic:**
1. Generate object key: `ordenes/{ordenId}/{uuid}.{ext}` where ext is derived from `tipoArchivo`
2. Create `S3Client` with R2 endpoint `https://{accountId}.r2.cloudflarestorage.com`
3. Use `S3Presigner.presignPutObject()` with 15-minute expiry
4. Compute `publicUrl = {r2PublicBaseUrl}/{objectKey}`
5. No DB write

**Response `200`:**
```json
{
  "presignedUrl": "https://<accountId>.r2.cloudflarestorage.com/<bucket>/<key>?X-Amz-...",
  "objectKey": "ordenes/abc123/550e8400-e29b.jpg",
  "publicUrl": "https://pub-<hash>.r2.dev/ordenes/abc123/550e8400-e29b.jpg"
}
```

---

### POST `/ordenes/{id}/multimedia/confirm`

**Request body:**
```json
{
  "objectKey": "ordenes/abc123/550e8400-e29b.jpg",
  "publicUrl": "https://pub-<hash>.r2.dev/ordenes/abc123/550e8400-e29b.jpg",
  "tipoArchivo": "image/jpeg",
  "descripcion": "Desgaste de transmision",
  "etapa": "diagnostico"
}
```

**Logic:**
1. Resolve `orden` by `id` (return `404` if not found)
2. Derive `categoria` from `tipoArchivo` (imagen / video / documento)
3. Insert `Multimedia` record:
   - `url = publicUrl`
   - `tipoArchivo`, `categoria`, `descripcion`, `etapa`
   - `usuarioId` from authenticated user
   - `ordenId`
4. Return `201` with full `MultimediaResponse`

**Response `201`:**
```json
{
  "id": "uuid",
  "usuarioId": "uuid",
  "usuario": "Rodrigo Soto",
  "tipoArchivo": "image/jpeg",
  "categoria": "imagen",
  "url": "https://pub-<hash>.r2.dev/ordenes/abc123/550e8400-e29b.jpg",
  "etapa": "diagnostico",
  "descripcion": "Desgaste de transmision",
  "createdAt": "2026-06-09T11:00:00-04:00"
}
```

---

## Backend: Spring Boot Configuration

### Dependencies (Maven/Gradle)
```xml
<!-- AWS SDK v2 S3 (R2 is S3-compatible) — use latest 2.x from Maven Central -->
<dependency>
  <groupId>software.amazon.awssdk</groupId>
  <artifactId>s3</artifactId>
</dependency>
<!-- Include BOM to manage version -->
<dependency>
  <groupId>software.amazon.awssdk</groupId>
  <artifactId>bom</artifactId>
  <version>2.27.21</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```

### `application.properties` / secrets
```properties
cloudflare.r2.account-id=${CF_R2_ACCOUNT_ID}
cloudflare.r2.access-key-id=${CF_R2_ACCESS_KEY_ID}
cloudflare.r2.secret-access-key=${CF_R2_SECRET_ACCESS_KEY}
cloudflare.r2.bucket-name=${CF_R2_BUCKET_NAME}
cloudflare.r2.public-base-url=${CF_R2_PUBLIC_BASE_URL}
```

### `R2Config.java` (bean)
```java
@Bean
public S3Client r2Client(R2Properties props) {
    return S3Client.builder()
        .endpointOverride(URI.create("https://" + props.getAccountId() + ".r2.cloudflarestorage.com"))
        .region(Region.of("auto"))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(props.getAccessKeyId(), props.getSecretAccessKey())
        ))
        .build();
}

@Bean
public S3Presigner r2Presigner(R2Properties props) {
    return S3Presigner.builder()
        .endpointOverride(URI.create("https://" + props.getAccountId() + ".r2.cloudflarestorage.com"))
        .region(Region.of("auto"))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(props.getAccessKeyId(), props.getSecretAccessKey())
        ))
        .build();
}
```

---

## Mobile: Changes to Existing Code

### `src/lib/api/upload-client.ts` — replace `uploadMultimedia`

```typescript
export async function uploadToR2(
  ordenId: string,
  fileUri: string,
  fields: { tipoArchivo: string; descripcion?: string; etapa?: string }
): Promise<MultimediaRecord> {
  // 1. Get presigned URL from backend
  const { presignedUrl, objectKey, publicUrl } = await httpClient.post<PresignResponse>(
    `ordenes/${ordenId}/multimedia/presign`,
    { tipoArchivo: fields.tipoArchivo, nombre: fileUri.split('/').pop() ?? 'file' }
  );

  // 2. PUT binary directly to R2 (no auth header — presigned URL is self-authenticated)
  const upload = await FileSystem.uploadAsync(presignedUrl, fileUri, {
    httpMethod: 'PUT',
    uploadType: FileSystem.FileSystemUploadType.BINARY_CONTENT,
    headers: { 'Content-Type': fields.tipoArchivo },
  });
  if (upload.status < 200 || upload.status >= 300) {
    throw new ApiError(upload.status, 'R2 upload failed');
  }

  // 3. Confirm with backend — creates DB record
  return httpClient.post<MultimediaRecord>(
    `ordenes/${ordenId}/multimedia/confirm`,
    { objectKey, publicUrl, ...fields }
  );
}
```

### `src/features/ordenes/services/ordenes.service.ts` — `addMultimedia()`

```typescript
async addMultimedia(id: string, payload: AddMultimediaPayload): Promise<void> {
  if (!payload.uri) return;
  await uploadToR2(id, payload.uri, {
    tipoArchivo: payload.tipoArchivo ?? payload.type,
    descripcion: payload.descripcion ?? '',
  });
}
```

**No other mobile files change.** `useAddMultimedia`, `MediaSheet`, `DetailScreen`, `media.ts` all stay the same.

---

## Error Handling

| Step | Failure | Result | UX |
|------|---------|--------|-----|
| presign call | Network/auth/server error | Exception thrown | `onError` toast: "Error al preparar subida" |
| R2 PUT | Wrong MIME, expired URL, file too large | `ApiError` thrown | `onError` toast: "Error al subir archivo" |
| confirm call | DB error after successful upload | Orphaned R2 file (benign) | `onError` toast: "Error al registrar archivo" |

Orphaned R2 files are low-risk with a public bucket. Periodic GC can clean them by comparing R2 object keys with DB records if needed — not in scope for this feature.

---

## Cloudflare R2 Setup (manual, one-time)

1. Create R2 bucket in Cloudflare dashboard
2. Enable **Public access** on the bucket (or add custom domain)
3. Note the `r2.dev` public URL
4. Create **R2 API token** with Object Read & Write on the bucket
5. Copy Account ID, Access Key ID, Secret Access Key into backend secrets

---

## Verification

1. Add backend env vars with real R2 credentials
2. Start backend + mobile dev server
3. Open an order → tap camera/gallery → select a file
4. Verify in R2 dashboard: file appears at `ordenes/{id}/{uuid}.ext`
5. Verify in DB: `multimedia` table row with correct `url`, `tipoArchivo`, `ordenId`
6. Verify timeline: multimedia event appears in order detail after query invalidation
7. Test error case: use expired presigned URL → confirm error toast appears without crashing
