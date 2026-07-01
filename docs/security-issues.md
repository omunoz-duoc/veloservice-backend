# Security Issues — veloservice-backend

Detected: 2026-05-30. Not yet fixed.

---

## [CRÍTICO] Privilege Escalation via Self-Registration

**File:** `src/main/java/com/veloservice/auth/interfaces/rest/AuthController.java`
**Endpoint:** `POST /auth/register`

Endpoint is public. If `AuthRegisterRequest` accepts `rol` or `sucursalId` fields, any anonymous user can self-register as admin/mechanic with arbitrary scope.

**Fix (choose one):**
- (a) Restrict with `@PreAuthorize("hasRole('ADMIN_TALLER') or hasRole('PLATAFORMA')")` and validate in `AuthService` that requested `rol`/`sucursalId` are within caller's scope.
- (b) If public registration is needed, hardcode assigned role to a low-privilege default (e.g. `CLIENTE`) and ignore client-supplied `rol`/`sucursalId`. Require authenticated admin to elevate afterward.

---

## [ALTO] IDOR / Missing Authorization Scoping on Orders

**File:** `src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java`
**Endpoints:** `GET /ordenes`, `GET /ordenes/{id}`

Neither endpoint verifies that the orders belong to the authenticated user's taller. A user from taller A can enumerate or fetch orders from taller B by guessing IDs.

**Fix:**
- Extract `tallerId`/`sucursalId` from JWT via `JwtTokenProvider` and pass as scope filter to `ordenService.listar(tallerId)`.
- In `obtener(id)`: after loading order, assert `orden.getTallerId().equals(callerTallerId)`, throw 403/404 otherwise.
- Add `@PreAuthorize` to restrict to appropriate roles.

---

## [MEDIO] Missing Password Strength Validation on Reset

**File:** `src/main/java/com/veloservice/auth/application/usecase/AuthService.java`
**Method:** `changePassword(String token, String newPassword)`

Password strength is not validated on reset flow. A user can reset to a trivially weak password even if `register()` enforces a policy.

**Fix:**
- Call `validatePassword(newPassword)` at the top of `changePassword()`, reusing the same policy as `register()`.
- Consider sending reset token via POST body instead of `?token=` query param to avoid referrer/log leakage.
