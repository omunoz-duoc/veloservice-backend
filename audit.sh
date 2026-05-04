#!/bin/bash
REPO="/workspaces/veloservice-backend"
SRC="$REPO/src/main/java"
RES="$REPO/src/main/resources"
OUT="$REPO/AUDITORIA.md"

echo "# AUDITORIA VELOSERVICE BACKEND" > "$OUT"
echo "Fecha: $(date)" >> "$OUT"
echo "" >> "$OUT"

echo "## 1. CONFIGURACION YML" >> "$OUT"
for f in application application-dev application-prod; do
  if [ -f "$RES/$f.yml" ]; then
    echo "### $f.yml (EXISTE)" >> "$OUT"
    echo '```yaml' >> "$OUT"
    cat "$RES/$f.yml" >> "$OUT"
    echo '```' >> "$OUT"
  else
    echo "### $f.yml (NO EXISTE)" >> "$OUT"
  fi
  echo "" >> "$OUT"
done

echo "## 2. ESTRUCTURA DE PAQUETES" >> "$OUT"
if command -v tree &> /dev/null; then
  tree -L 4 -d "$SRC" >> "$OUT" 2>/dev/null || find "$SRC" -maxdepth 4 -type d | sed "s|$SRC/||" | sort >> "$OUT"
else
  find "$SRC" -maxdepth 4 -type d | sed "s|$SRC/||" | sort >> "$OUT"
fi
echo "" >> "$OUT"

echo "## 3. ENTIDADES JPA (@Entity)" >> "$OUT"
grep -rl "@Entity" "$SRC" 2>/dev/null | while read -r f; do
  CLASS=$(basename "$f" .java)
  PKG=$(grep -m1 "^package " "$f" | sed 's/package //;s/;//')
  TABLE=$(grep -o '@Table(name = "[^"]*"\|@Table(name="[^"]*"' "$f" | head -1 | grep -o '"[^"]*"')
  ID=$(grep -c "@Id" "$f")
  AUDIT=$(grep -c "created_at\|updated_at\|createdAt\|updatedAt" "$f")
  ENUMS=$(grep -c "@Enumerated\|enum " "$f")
  REL=$(grep -o "@ManyToOne\|@OneToMany\|@OneToOne" "$f" | sort -u | tr '\n' ',' | sed 's/,$//')
  echo "- **$CLASS** | pkg: \`$PKG\` | tabla: $TABLE | @Id: $ID | auditoria: $AUDIT | enums: $ENUMS | relaciones: [$REL]" >> "$OUT"
done
echo "" >> "$OUT"

echo "## 4. REPOSITORIES" >> "$OUT"
grep -rl "@Repository" "$SRC" 2>/dev/null | while read -r f; do
  CLASS=$(basename "$f" .java)
  TYPE="interface"
  grep -q "^public class $CLASS" "$f" && TYPE="clase"
  EXT=$(grep -o "extends [A-Za-z0-9<>,_]*" "$f" | head -1)
  IMPL=$(find "$SRC" -name "${CLASS}Impl.java" | wc -l)
  echo "- **$CLASS** | tipo: $TYPE | $EXT | Impl: $IMPL" >> "$OUT"
done
echo "" >> "$OUT"

echo "## 5. SEGURIDAD JWT" >> "$OUT"
TK=$(find "$SRC" -name "JwtTokenProvider.java" | head -1)
if [ -n "$TK" ]; then
  echo "### JwtTokenProvider claims" >> "$OUT"
  echo '```java' >> "$OUT"
  grep -A 20 "generateToken\|createToken\|claims\|put(" "$TK" | head -30 >> "$OUT"
  echo '```' >> "$OUT"
else
  echo "- JwtTokenProvider.java: NO ENCONTRADO" >> "$OUT"
fi

FL=$(find "$SRC" -name "JwtAuthenticationFilter.java" | head -1)
if [ -n "$FL" ]; then
  echo "### JwtAuthenticationFilter" >> "$OUT"
  echo '```java' >> "$OUT"
  grep -A 15 "doFilterInternal\|setAttribute\|sucursal\|taller" "$FL" | head -30 >> "$OUT"
  echo '```' >> "$OUT"
else
  echo "- JwtAuthenticationFilter.java: NO ENCONTRADO" >> "$OUT"
fi

ASP=$(find "$SRC" -name "TenantOperationAspect.java" | head -1)
if [ -n "$ASP" ]; then
  echo "### TenantOperationAspect SQL" >> "$OUT"
  echo '```java' >> "$OUT"
  grep -A 10 "SET SESSION\|set_config\|sucursal\|taller" "$ASP" | head -20 >> "$OUT"
  echo '```' >> "$OUT"
else
  echo "- TenantOperationAspect.java: NO ENCONTRADO" >> "$OUT"
fi

AUTH=$(find "$SRC" -name "AuthController.java" | head -1)
if [ -n "$AUTH" ]; then
  echo "### AuthController endpoints" >> "$OUT"
  grep -o '@RequestMapping("[^"]*"\|@PostMapping("[^"]*"\|@GetMapping("[^"]*"' "$AUTH" | sort -u >> "$OUT"
  echo "" >> "$OUT"
  echo "### AuthController setup SaaS?" >> "$OUT"
  grep -n "setup\|crearTaller\|init\|bootstrap" "$AUTH" | head -5 >> "$OUT"
else
  echo "- AuthController.java: NO ENCONTRADO" >> "$OUT"
fi
echo "" >> "$OUT"

echo "## 6. FLYWAY MIGRATIONS" >> "$OUT"
if [ -d "$RES/db/migration" ]; then
  ls -1 "$RES/db/migration"/*.sql 2>/dev/null | while read -r f; do
    NAME=$(basename "$f")
    TABLES=$(grep -oi "create table [a-z_]*" "$f" | sed 's/create table //' | sort -u | tr '\n' ',' | sed 's/,$//')
    echo "- **$NAME** | tablas: $TABLES" >> "$OUT"
  done
else
  echo "- directorio db/migration NO EXISTE" >> "$OUT"
fi
echo "" >> "$OUT"

echo "## 7. ENUMS JAVA" >> "$OUT"
grep -rl "^public enum" "$SRC" 2>/dev/null | while read -r f; do
  echo "- $(basename "$f" .java) ($(grep -m1 "^package " "$f" | sed 's/package //;s/;//'))" >> "$OUT"
done
echo "" >> "$OUT"

echo "## 8. CONTROLLERS (@RestController)" >> "$OUT"
grep -rl "@RestController" "$SRC" 2>/dev/null | while read -r f; do
  CLASS=$(basename "$f" .java)
  BASE=$(grep -o '@RequestMapping("[^"]*"' "$f" | head -1)
  DTO=$(grep -c "ResponseEntity<.*Dto\|ResponseEntity<.*DTO\|return new .*Response\|return .*Request" "$f")
  ENT=$(grep -c "return .*Repository\|return .*Service\|return .*Entity\|ResponseEntity.ok(.*[a-z]" "$f")
  echo "- **$CLASS** | base: $BASE | usa DTOs: $DTO | devuelve entidades: $ENT" >> "$OUT"
done
echo "" >> "$OUT"

echo "## 9. POSIBLE BASURA / OBSOLETO" >> "$OUT"
for cls in TallerCliente AplicarMembresiaRequest MembresiaRequest; do
  FOUND=$(find "$SRC" -name "$cls.java" | wc -l)
  if [ "$FOUND" -gt 0 ]; then
    REFS=$(grep -rl "$cls" "$SRC" 2>/dev/null | grep -v "$cls.java" | wc -l)
    echo "- **$cls.java** | referencias externas: $REFS" >> "$OUT"
  fi
done
echo "" >> "$OUT"

echo "## 10. POM.XML (dependencias clave)" >> "$OUT"
echo '```xml' >> "$OUT"
grep -A 1 "<artifactId>spring-boot-starter\|postgresql\|flyway\|jjwt\|lombok\|security" "$REPO/pom.xml" | head -30 >> "$OUT"
echo '```' >> "$OUT"

echo "" >> "$OUT"
echo "--- FIN AUDITORIA ---" >> "$OUT"
echo "Reporte generado en: $OUT"
