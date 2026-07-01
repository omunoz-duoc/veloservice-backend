# Mobile Endpoint Audit & Implementation Plan

## Context

`docs/endpoint-documentation.md` is the mobile API contract. Audit of the backend (branch `feat/mobile-endpoints`, global prefix `/api/v1`) against it found:

**Missing endpoints:** GET `/ordenes/urgentes`, DELETE `/ordenes/{id}`, PATCH `/ordenes/bulk`, POST `/auth/verify-code`, the entire DASHBOARD section (9 endpoints), PUT `/mecanicos/{id}`.

**Shape gaps in existing endpoints:** `AuthResponse` lacks `id`; `/ordenes/tipos` lacks `codigo`; PATCH `/ordenes/{id}` lacks `fechaEstimada`/`notasInternas`; productos/finanzas/clientes return snake_case where doc shows camelCase; `mecanicos/activos` returns a slim shape vs the doc's rich one; POST `/servicios` doc shape differs entirely; POST `/ordenes` doc sends `tipoTrabajo` as codigo string but backend expects UUID (frontend can resolve via `/ordenes/tipos`).

**User decisions:** Skip dashboard module. Skip mecánicos enrichment and PUT. Fix critical gaps only (no snake_case renames, no clientes/servicios reshaping — doc says existing endpoints don't count). Build all 4 missing endpoints.

**Bugs found during audit (fixed as part of this work):**
- V3 migration's `prioridad` CHECK constraint allows only `('alta','media','baja')` — `urgente` (present in `PrioridadOrdenEnum` and the prioridades catalog) violates it. Must fix or `/ordenes/urgentes` never matches in prod.
- `AuthService.resetPassword` builds `PasswordResetToken` without `userId`, `used`, `createdAt` (all required; `@ManyToOne usuario` is `insertable=false`).

## Scope

### A. New endpoints (4)
### B. Critical fixes (3): `AuthResponse.id`, tipos `codigo`, PATCH `fechaEstimada`+`notasInternas`

---

## Step 0 — Migration V7 (latest is V6)

Create `src/main/resources/db/migration/V7__mobile_endpoint_fields.sql`:

```sql
ALTER TABLE ordenes ADD COLUMN IF NOT EXISTS notas_internas TEXT;

ALTER TABLE ordenes DROP CONSTRAINT IF EXISTS ordenes_prioridad_check;
ALTER TABLE ordenes ADD CONSTRAINT ordenes_prioridad_check
    CHECK (prioridad IN ('baja', 'media', 'alta', 'urgente'));
```

Add to `Orden.java`: `@Column(name = "notas_internas", columnDefinition = "TEXT") private String notasInternas;`

## Step 1 — `codigo` in GET /ordenes/tipos

`OrdenController.java:86`: `TipoOrdenResponse(String id, String codigo, String nombre)`; mapping adds `t.getCodigo()` (entity `TipoOrden` already has unique `codigo`).

## Step 2 — `id` in AuthResponse

- `AuthLoginResult` + `AuthResponse`: add `UUID id` as first field (both positional `@AllArgsConstructor`).
- `AuthService`: pass `usuario.getId()` in `login` (~line 94) and `loginPlataforma` (~126), `saved.getId()` in `register` (~170).
- `AuthMapper.toResponse`: prepend `result.getId()`.
- Compiler flags test constructor sites (`AuthControllerTest`, `AuthServiceTest`) — update.

## Step 3 — PATCH /ordenes/{id}: `fechaEstimada` + `notasInternas`

- `OrdenUpdateRequest` + `OrdenUpdateCommand`: add `LocalDate fechaEstimada`, `String notasInternas` (append at end of command; update construction site in `OrdenController.actualizar` ~196–208).
- `OrdenService.actualizar`: after `mecanicoId` block:
  - `fechaEstimada` → `orden.setFechaPrometida(fecha.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime())` (same conversion as `crearEnSucursal` ~line 845; no separate `fecha_estimada` column exists — maps to `fecha_prometida`).
  - `notasInternas` → `orden.setNotasInternas(normalizarOpcional(value, 4000, "notasInternas"))`.
- Do NOT add `notasInternas` to read responses (out of scope; would require threading through `DETALLE_SELECT`/`OrdenDetalleResult` chain).

## Step 4 — GET /ordenes/urgentes

Service (`OrdenService`), reusing tenant-scoped `listar(UUID requestedSucursalId)`:

```java
private static final Set<String> PRIORIDADES_URGENTES = Set.of("urgente", "alta");
private static final Set<String> ESTADOS_TERMINALES = Set.of("entregada", "cancelada");

@TenantOperation @Transactional(readOnly = true)
public List<OrdenReadResult> listarUrgentes(UUID requestedSucursalId) {
    return listar(requestedSucursalId).stream()
            .filter(o -> o.prioridad() != null && PRIORIDADES_URGENTES.contains(o.prioridad()))
            .filter(o -> !ESTADOS_TERMINALES.contains(o.estadoCodigo()))
            .toList();
}
```

Controller: `@GetMapping("/urgentes")` returning `OrdenReadListResponse(total, ordenes)` — exact doc shape. Includes both `urgente` and `alta` (existing data can only contain `alta` pre-V7). In-memory filter matches existing `metricas()` pattern.

## Step 5 — DELETE /ordenes/{id} — soft delete via `cancelada` estado

Hard delete rejected: FK children (`cobros`, `garantias`, `multimedia`, `orden_estados`) hold financial/audit history. Estados catalog already has `cancelada` (es_final=TRUE), and existing guards (`aplicarCambioEstado`, `validarOrdenPermiteModificar*`) block further mutation of cancelled orders.

Service `anular(String id)`: `requerirUsuarioContext()` → `buscarOrdenParaMutacion(id)` (tenant-scoped, 404) → if already `cancelada`, return (idempotent) → `aplicarCambioEstado(orden, "cancelada", "Orden eliminada", now, usuarioId)` (throws 409 on `entregada` via final-state guard — correct) → save.

Controller:

```java
@DeleteMapping("/{id}")
@PreAuthorize("hasRole('admin_taller') or hasRole('jefe_taller')")
public ResponseEntity<Void> eliminar(@PathVariable String id) {
    ordenService.anular(id);
    return ResponseEntity.noContent().build();   // 204, empty per doc
}
```

Note: cancelled orders still appear in GET /ordenes — mobile filters by `estado.codigo` (out of scope to change).

## Step 6 — PATCH /ordenes/bulk

New DTO `OrdenBulkUpdateRequest`: `@NotEmpty @Size(max=50) List<String> ids` (String — endpoints accept UUID or numero_orden), optional `String estado`, optional `UUID mecanicoId`. Response record `OrdenBulkUpdateResponse(int total, List<UUID> ids)` (doc doesn't specify a response).

Service `actualizarBulk(ids, estadoCodigo, mecanicoId)` — **atomic**, single `@Transactional`:
- 400 if both estado and mecanicoId absent; 404 if mecanicoId not an active mechanic (`usuarioRepository.existsActiveMecanicoById`).
- Per id: `buscarOrdenParaMutacion(id)` (per-order tenant scoping), dedupe, `aplicarCambioEstado(...)` if estado present (reuses history writing; 404/409 rolls back whole batch with failing order in message), set mecanicoId, save.

Controller: `@PatchMapping("/bulk")` with `@Valid` body; keep class-level roles (mecanicos already may `cambiarEstado`).

Note for doc owner: doc example `"estado": "en_proceso"` is not a catalog code (catalog: `recibida`, `en_diagnostico`, `esperando_repuestos`, `en_reparacion`, `control_calidad`, `lista_para_entrega`, `entregada`, `cancelada`) — will 404, consistent with existing PATCH `/{id}/estado`.

## Step 7 — POST /auth/verify-code

Design: **the 6-digit code IS the reset token** — minimal change, no migration (`token_hash` stores SHA-256 hex of anything), existing `change-password {token, newPassword}` works with the code, and codeless-email lookup works because `token_hash` is globally UNIQUE. Entropy mitigations: 15-min expiry (existing `jwt.reset-expiration:900000`), one active token per user (`deleteByUsuarioId` already called), uniqueness retry, existing `PasswordResetRateLimiter` on issuance. Recommended hardening (same PR if cheap): per-IP rate limit on verify-code.

1. `AuthService`: replace `generateResetToken()` with `String.format("%06d", SECURE_RANDOM.nextInt(1_000_000))`; retry up to 5× on `existsByTokenHash` collision; **fix latent builder bug** — set `.userId(usuario.getId())`, `.used(false)`, `.createdAt(now)`.
2. New `AuthService.verifyResetCode(String code)`: `findByTokenHashAndUsedFalseAndExpiresAtAfter(hashToken(code.trim()), now).isPresent()` — does NOT mark used; change-password consumes it.
3. `PasswordResetTokenRepository`: add `boolean existsByTokenHash(String tokenHash)`.
4. New `AuthVerifyCodeRequest`: `@NotBlank @Pattern(regexp = "\\d{6}") String code`.
5. `AuthController`: `@PostMapping("/verify-code")` → `ResponseEntity<Boolean>` (doc: bare `true`).
6. `SecurityConfig.java:61`: add `"/auth/verify-code"` to permitAll.
7. `ResendEmailService.sendPasswordResetEmail`: show the code prominently in the email; keep the reset link (`?token=<code>`) so web flow is unchanged.

## Step 8 — Tests

Follow existing pattern: `@WebMvcTest(Controller.class)` + `@AutoConfigureMockMvc(addFilters = false)` + `@WithMockUser` + `@MockBean` service/`JwtTokenProvider`/`UsuarioRepository` (mirror `OrdenControllerErrorHandlingTest`).

- New `OrdenControllerMobileEndpointsTest`: urgentes 200 shape; DELETE 204 + role 403 + ConflictException→409; bulk 200/$.total + empty ids→400 + BadRequest→400; tipos asserts `$[0].codigo`.
- Extend `AuthControllerTest`: login asserts `$.id`; verify-code true/false; bad format→400.
- Extend `AuthServiceTest`: verifyResetCode found/expired/used; resetPassword persists 6-digit code with userId/used/createdAt set.
- OrdenService tests: actualizar maps fechaEstimada→fechaPrometida + notasInternas; listarUrgentes filtering; actualizarBulk happy/400; anular idempotency.

## Order of work

1. V7 migration + `Orden.notasInternas`
2. tipos `codigo` (isolated)
3. `AuthResponse.id` (isolated)
4. PATCH fields
5. GET /ordenes/urgentes
6. DELETE /ordenes/{id}
7. PATCH /ordenes/bulk
8. POST /auth/verify-code (+ SecurityConfig + email)
9. Tests, then full `./mvnw test` (48 existing tests must stay green; positional-constructor changes ripple into tests — compiler-guided)

## Verification

- `./mvnw test` — all green.
- Manual smoke (app + local Postgres): login → assert `id` in response; GET `/api/v1/ordenes/tipos` → `codigo` present; create order with `prioridad: "urgente"` (validates V7 constraint) → appears in GET `/api/v1/ordenes/urgentes`; DELETE it as admin_taller → 204, estado `cancelada`, second DELETE → 204 (idempotent); PATCH `/api/v1/ordenes/bulk` with 2 ids + estado → both updated with history rows; POST `/auth/reset-password` → email contains 6-digit code → POST `/auth/verify-code` → `true` → `/auth/change-password` with code works.

## Out of scope (flagged, per user decision)

- Dashboard module (9 endpoints) — mobile uses existing `ordenes/metricas`, `productos/alertas`, `finanzas/metricas`.
- Mecánicos enrichment (iniciales/color/bahia/horas/estado/capacidad) + PUT /mecanicos/{id}.
- snake_case→camelCase renames (productos, finanzas, clientes), clientes extra fields, POST /servicios reshaping.
- POST /ordenes `tipoTrabajo` stays UUID — mobile resolves codigo→id via `/ordenes/tipos` (now exposing `codigo`).

## Critical files

- `src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java`
- `src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java`
- `src/main/java/com/veloservice/ordenes/interfaces/rest/dto/OrdenUpdateRequest.java` (+ new bulk DTOs)
- `src/main/java/com/veloservice/ordenes/domain/model/Orden.java`
- `src/main/java/com/veloservice/auth/application/usecase/AuthService.java`
- `src/main/java/com/veloservice/auth/interfaces/rest/{AuthController,AuthResponse}.java` (+ new AuthVerifyCodeRequest)
- `src/main/java/com/veloservice/config/security/SecurityConfig.java`
- `src/main/resources/db/migration/V7__mobile_endpoint_fields.sql` (new)
