# Photo Upload via Cloudflare R2 — Design Spec
Date: 2026-05-19
Author: Oscar Munoz

## Overview

Wire Cloudflare R2 blob storage into the existing `multimedia` layer so that mechanics
can attach before/during/after photos to service orders from the Expo mobile app.

The `multimedia` table and `MultimediaService` already exist. The `url` column currently
stores arbitrary strings. This spec replaces that with a real upload flow backed by R2.

## Scope

- Entity: `ordenes` only (`multimedia` table, existing `Multimedia` entity)
- Flow: presigned PUT (Expo uploads directly to R2, Spring never handles file bytes)
- Access: public R2 bucket (permanent public URLs stored in DB)
- File types: JPEG and PNG only
- Size limit: 10 MB per file

## Data Flow

```
Expo App                       Spring Backend                  Cloudflare R2
   │                                  │                              │
   ├─ POST /ordenes/{id}/multimedia/presign                          │
   │   { fileName, contentType, fileSize }                          │
   │                                  │── validate: type=jpg/png    │
   │                                  │── validate: size ≤ 10MB     │
   │                                  │── generate fileKey          │
   │                                  │── S3 presignPutObject ──────►│
   │◄─ { uploadUrl, fileKey } ────────│                              │
   │                                  │                              │
   ├─ PUT uploadUrl  (direct, no Spring) ───────────────────────────►│
   │◄─ 200 OK ───────────────────────────────────────────────────────│
   │                                  │                              │
   └─ POST /ordenes/{id}/multimedia/confirm                          │
       { fileKey, tipoArchivo, etapa, descripcion? }                │
                                      │── construct publicUrl        │
                                      │── MultimediaService.crear()  │
                                      │── INSERT multimedia row      │
                                     ◄│                              │
                        Returns MultimediaResponse                   │
```

**fileKey format:** `ordenes/{ordenId}/{uuid}.{ext}`
**publicUrl formula:** `https://{R2_PUBLIC_DOMAIN}/{fileKey}`

## Backend Changes

### New Maven dependency
```xml
<dependency>
  <groupId>software.amazon.awssdk</groupId>
  <artifactId>s3</artifactId>
  <version>2.26.x</version>
</dependency>
```

### New configuration (`application.yml` + profiles)
```yaml
r2:
  account-id: ${R2_ACCOUNT_ID}
  access-key: ${R2_ACCESS_KEY}
  secret-key: ${R2_SECRET_KEY}
  bucket: ${R2_BUCKET}
  public-domain: ${R2_PUBLIC_DOMAIN}
  presign-expiry-minutes: 10
```

### New files

| File | Purpose |
|------|---------|
| `config/storage/R2Properties.java` | `@ConfigurationProperties("r2")` record |
| `config/storage/R2Config.java` | Builds `S3Client` + `S3Presigner` with R2 endpoint override |
| `ordenes/application/usecase/StorageService.java` | Interface: `presign(fileKey, contentType, expiryMinutes) → URL` |
| `ordenes/infraestructure/storage/R2StorageService.java` | Implements `StorageService` via `S3Presigner` |

### R2 endpoint
```
https://{R2_ACCOUNT_ID}.r2.cloudflarestorage.com
```
Set as `endpointOverride` on the `S3Client` in `R2Config`.

### New endpoints (added to `OrdenController`, follow existing pattern)

**`POST /ordenes/{id}/multimedia/presign`**
- Request: `{ fileName: String, contentType: String, fileSize: Long }`
- Validates: `contentType` in `[image/jpeg, image/png]`, `fileSize <= 10_485_760`
- Returns `400` with message if invalid
- Returns `200`: `{ uploadUrl: String, fileKey: String }`

**`POST /ordenes/{id}/multimedia/confirm`**
- Request: `{ fileKey: String, tipoArchivo: TipoArchivoEnum, etapa: EtapaMultimediaEnum, descripcion?: String }`
- Constructs `publicUrl = "https://{R2_PUBLIC_DOMAIN}/{fileKey}"`
- Delegates to `MultimediaService.crear()` (existing method)
- Returns `200`: `MultimediaResponse`

### MultimediaService new methods
- `generatePresign(ordenId, fileName, contentType, fileSize)` — validates, builds fileKey, calls `StorageService.presign()`
- `confirm(ordenId, fileKey, MultimediaCreateCommand)` — constructs public URL, calls `crear()`

### No Flyway migration needed
`multimedia.url` already exists as `TEXT`. No schema changes required.

## Frontend Changes (Expo)

### Dependencies
```bash
npx expo install expo-image-picker expo-file-system
```

### Upload flow
```typescript
// 1. Pick photo
const result = await ImagePicker.launchImageLibraryAsync({
  mediaTypes: ImagePicker.MediaTypeOptions.Images,
  quality: 0.8,
  allowsEditing: true,
});

// 2. Get presigned URL
const { uploadUrl, fileKey } = await api.post(
  `/ordenes/${ordenId}/multimedia/presign`,
  { fileName: result.fileName, contentType: 'image/jpeg', fileSize: result.fileSize }
);

// 3. Upload directly to R2
await fetch(uploadUrl, {
  method: 'PUT',
  headers: { 'Content-Type': 'image/jpeg' },
  body: await FileSystem.readAsStringAsync(result.uri, { encoding: 'base64' }),
});

// 4. Confirm to backend
const multimedia = await api.post(
  `/ordenes/${ordenId}/multimedia/confirm`,
  { fileKey, tipoArchivo: 'FOTO', etapa: selectedEtapa }
);

// 5. Display
<Image source={{ uri: multimedia.url }} />
```

### Critical notes
- `Content-Type` on the R2 PUT must **exactly match** what was sent to `/presign` — mismatch = 403
- No `Authorization` header on the R2 PUT — presigned URL carries credentials in query params
- Use `quality: 0.8` on ImagePicker to avoid hitting 10 MB limit on high-res devices
- If PUT succeeds but `/confirm` fails, the R2 object is orphaned — acceptable for now

## Cloudflare R2 Setup (one-time manual)

1. **Create bucket** — Cloudflare Dashboard → R2 → Create bucket (`veloservice-media`)
2. **Enable public access** — Bucket → Settings → Public Access → Allow Access. Use `*.r2.dev` subdomain or connect custom domain via CNAME → set as `R2_PUBLIC_DOMAIN`
3. **Create API token** — R2 → Manage R2 API Tokens → Object Read & Write on the bucket only. Save Account ID, Access Key ID, Secret Access Key
4. **Set CORS on bucket:**
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
5. **Wire into Cloud Run env vars:** `R2_ACCOUNT_ID`, `R2_ACCESS_KEY`, `R2_SECRET_KEY`, `R2_BUCKET`, `R2_PUBLIC_DOMAIN`

## Testing Strategy

### Backend unit tests (follow existing `@WebMvcTest` + `resolveOrdenId` stub pattern)
- `MultimediaService` presign: mock `StorageService`, verify fileKey format `ordenes/{id}/...`, verify 400 on invalid type or oversized file
- `MultimediaService` confirm: mock `StorageService` + `MultimediaRepository`, verify public URL construction
- Controller: mock `MultimediaService`, verify HTTP 200/201/400 response shapes

### StorageService test isolation
`StorageService` is an interface — tests inject a mock. No test touches real R2.

### Local dev
Under `dev` profile, wire a no-op `StorageService` bean that returns a dummy presign URL.

### Manual end-to-end checklist
1. Call `/presign` → `uploadUrl` starts with `*.r2.cloudflarestorage.com`
2. `curl -X PUT <uploadUrl> --data-binary @photo.jpg -H "Content-Type: image/jpeg"` → 200
3. Call `/confirm` → row in `multimedia` table, `url` = public domain URL
4. Load URL in browser → photo renders correctly

## Out of Scope
- Cleanup of orphaned R2 objects
- Private bucket / presigned read URLs
- Video or PDF uploads
- Productos, clientes, or profile photo uploads
- Image resizing / thumbnail generation
