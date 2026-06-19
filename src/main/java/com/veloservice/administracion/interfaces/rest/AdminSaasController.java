package com.veloservice.administracion.interfaces.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.veloservice.administracion.domain.model.Modulo;
import com.veloservice.administracion.domain.model.PlanSaas;
import com.veloservice.administracion.domain.model.Taller;
import com.veloservice.administracion.infraestructure.persistence.repository.ModuloRepository;
import com.veloservice.administracion.infraestructure.persistence.repository.PlanSaasRepository;
import com.veloservice.administracion.infraestructure.persistence.repository.SucursalRepository;
import com.veloservice.administracion.infraestructure.persistence.repository.TallerRepository;
import com.veloservice.auth.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.BicicletaRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.ClienteRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.MembresiaRepository;
import com.veloservice.finanzas.infraestructure.persistence.repository.CobroRepository;
import com.veloservice.inventario.infraestructure.persistence.repository.ProductoRepository;
import com.veloservice.notificaciones.domain.EstadoNotificacionEnum;
import com.veloservice.notificaciones.infraestructure.persistence.repository.NotificacionRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.GarantiaRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import com.veloservice.proveedores_compras.infraestructure.persistence.repository.CompraRepository;
import com.veloservice.proveedores_compras.infraestructure.persistence.repository.SucursalProveedorRepository;
import com.veloservice.servicios.infraestructure.persistence.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('plataforma')")
@RequiredArgsConstructor
public class AdminSaasController {

    private final TallerRepository tallerRepository;
    private final PlanSaasRepository planSaasRepository;
    private final ModuloRepository moduloRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final BicicletaRepository bicicletaRepository;
    private final ServicioRepository servicioRepository;
    private final ProductoRepository productoRepository;
    private final SucursalProveedorRepository sucursalProveedorRepository;
    private final CompraRepository compraRepository;
    private final GarantiaRepository garantiaRepository;
    private final MembresiaRepository membresiaRepository;
    private final CobroRepository cobroRepository;
    private final NotificacionRepository notificacionRepository;
    private final SucursalRepository sucursalRepository;
    private final OrdenRepository ordenRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/talleres")
    public List<AdminTallerResponse> listarTalleres() {
        Map<UUID, PlanSaas> planesById = planSaasRepository.findAll().stream()
                .collect(Collectors.toMap(PlanSaas::getId, Function.identity()));
        OffsetDateTime inicioMes = inicioMesActual();

        return tallerRepository.findAll().stream()
                .map(taller -> toTallerResponse(taller, planesById.get(taller.getPlanId()), inicioMes))
                .toList();
    }

    @GetMapping("/talleres/{id}")
    public AdminTallerResponse obtenerTaller(@PathVariable UUID id) {
        Taller taller = tallerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Taller no encontrado"));
        PlanSaas plan = taller.getPlanId() != null
                ? planSaasRepository.findById(taller.getPlanId()).orElse(null)
                : null;
        return toTallerResponse(taller, plan, inicioMesActual(), toOperacionResponse(taller.getId()));
    }

    @GetMapping("/modulos")
    public List<AdminModuloResponse> listarModulos() {
        return moduloRepository.findAllByActivoTrueOrderByNombreAsc().stream()
                .map(this::toModuloResponse)
                .toList();
    }

    @GetMapping("/planes")
    public List<AdminPlanResponse> listarPlanes() {
        return planSaasRepository.findAllByActivoTrueOrderByOrdenAsc().stream()
                .map(this::toPlanResponse)
                .toList();
    }

    @PostMapping("/planes")
    public AdminPlanResponse crearPlan(@RequestBody AdminCrearPlanRequest request) {
        validarCrearPlan(request);
        String codigo = normalizePlanCode(request.codigo());
        if (planSaasRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un plan con ese codigo");
        }

        PlanSaas plan = PlanSaas.builder()
                .codigo(codigo)
                .nombre(request.nombre().trim())
                .descripcion(normalizeOptional(request.descripcion()))
                .orden(request.orden())
                .activo(request.activo() == null || request.activo())
                .maxSucursales(request.maxSucursales())
                .maxUsuarios(request.maxUsuarios())
                .maxOrdenesMes(request.maxOrdenesMes())
                .precioMensual(request.precioMensual())
                .precioAnual(request.precioAnual())
                .trialDias(request.trialDias())
                .features(normalizeFeatures(request.features()))
                .build();

        return toPlanResponse(planSaasRepository.save(plan));
    }

    @PostMapping("/talleres")
    public AdminTallerResponse crearTaller(@RequestBody AdminCrearTallerRequest request) {
        validarCrearTaller(request);
        PlanSaas plan = planSaasRepository.findById(request.planId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan no encontrado"));

        OffsetDateTime now = OffsetDateTime.now();
        Taller taller = Taller.builder()
                .nombre(request.nombre().trim())
                .rut(request.rut().trim())
                .telefono(normalizeOptional(request.telefono()))
                .email(normalizeOptional(request.email()))
                .planId(plan.getId())
                .activo(request.activo())
                .createdAt(now)
                .updatedAt(now)
                .build();

        Taller saved = tallerRepository.save(taller);
        return toTallerResponse(saved, plan, inicioMesActual());
    }

    @PatchMapping("/talleres/{id}/estado")
    public AdminTallerResponse actualizarEstadoTaller(
            @PathVariable UUID id,
            @RequestBody AdminActualizarEstadoTallerRequest request
    ) {
        Taller taller = tallerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Taller no encontrado"));
        taller.setActivo(request.activo());
        taller.setUpdatedAt(OffsetDateTime.now());
        Taller saved = tallerRepository.save(taller);
        PlanSaas plan = saved.getPlanId() != null
                ? planSaasRepository.findById(saved.getPlanId()).orElse(null)
                : null;
        return toTallerResponse(saved, plan, inicioMesActual());
    }

    @GetMapping("/suscripciones")
    public List<AdminSuscripcionResponse> listarSuscripciones() {
        Map<UUID, PlanSaas> planesById = planSaasRepository.findAll().stream()
                .collect(Collectors.toMap(PlanSaas::getId, Function.identity()));

        return tallerRepository.findAll().stream()
                .map(taller -> toSuscripcionResponse(taller, planesById.get(taller.getPlanId())))
                .toList();
    }

    @GetMapping("/metrics/saas-kpis")
    public AdminSaasKpisResponse obtenerSaasKpis() {
        List<Taller> talleres = tallerRepository.findAll();
        Map<UUID, PlanSaas> planesById = planSaasRepository.findAll().stream()
                .collect(Collectors.toMap(PlanSaas::getId, Function.identity()));
        OffsetDateTime inicioMes = inicioMesActual();

        long talleresActivos = talleres.stream()
                .filter(taller -> Boolean.TRUE.equals(taller.getActivo()))
                .count();
        long talleresNuevosMes = talleres.stream()
                .filter(taller -> taller.getCreatedAt() != null && !taller.getCreatedAt().isBefore(inicioMes))
                .count();

        return new AdminSaasKpisResponse(
                talleres.size(),
                talleresActivos,
                talleresNuevosMes,
                calcularMrrTotal(talleres, planesById),
                "N/A",
                "N/A",
                "N/A"
        );
    }

    @GetMapping("/metrics/historical")
    public AdminMetricasDetalleResponse obtenerMetricasHistoricas() {
        List<Taller> talleres = tallerRepository.findAll();
        Map<UUID, PlanSaas> planesById = planSaasRepository.findAll().stream()
                .collect(Collectors.toMap(PlanSaas::getId, Function.identity()));

        return new AdminMetricasDetalleResponse(
                List.of(),
                List.of(),
                List.of(),
                distribucionPlanes(talleres, planesById),
                conteosPorTaller(talleres, TipoConteo.USUARIOS),
                conteosPorTaller(talleres, TipoConteo.CLIENTES),
                conteosPorTaller(talleres, TipoConteo.SUCURSALES),
                conteosPorTaller(talleres, TipoConteo.ORDENES)
        );
    }

    private AdminTallerResponse toTallerResponse(Taller taller, PlanSaas plan, OffsetDateTime inicioMes) {
        return toTallerResponse(taller, plan, inicioMes, null);
    }

    private AdminTallerResponse toTallerResponse(
            Taller taller,
            PlanSaas plan,
            OffsetDateTime inicioMes,
            AdminTallerOperacionResponse operacion
    ) {
        boolean activo = Boolean.TRUE.equals(taller.getActivo());
        String planNormalizado = normalizePlanCodigo(plan != null ? plan.getCodigo() : null);
        return new AdminTallerResponse(
                taller.getId(),
                taller.getNombre(),
                taller.getRut(),
                taller.getTelefono(),
                taller.getEmail(),
                taller.getLogoUrl(),
                activo,
                activo ? "activo" : "inactivo",
                taller.getPlanId(),
                planNormalizado,
                plan != null ? plan.getCodigo() : null,
                plan != null ? plan.getNombre() : null,
                taller.getCreatedAt(),
                null,
                (int) usuarioRepository.countByTallerId(taller.getId()),
                (int) ordenRepository.countByTallerIdAndFechaIngresoGreaterThanEqual(taller.getId(), inicioMes),
                List.of(),
                operacion
        );
    }

    private AdminTallerOperacionResponse toOperacionResponse(UUID tallerId) {
        return new AdminTallerOperacionResponse(
                sucursalRepository.countByTallerId(tallerId),
                usuarioRepository.countByTallerId(tallerId),
                clienteRepository.countByTallerId(tallerId),
                bicicletaRepository.countByClienteTallerId(tallerId),
                servicioRepository.countByTallerId(tallerId),
                productoRepository.countByTallerId(tallerId),
                sucursalProveedorRepository.countByTallerId(tallerId),
                compraRepository.countByTallerId(tallerId),
                ordenRepository.countByTallerId(tallerId),
                garantiaRepository.countByTallerId(tallerId),
                membresiaRepository.countByTallerId(tallerId),
                cobroRepository.countByTallerId(tallerId),
                notificacionRepository.countByTallerIdAndEstado(tallerId, EstadoNotificacionEnum.pendiente)
        );
    }

    private AdminModuloResponse toModuloResponse(Modulo modulo) {
        return new AdminModuloResponse(
                modulo.getId(),
                modulo.getNombre(),
                modulo.getDescripcion(),
                modulo.getRuta(),
                Boolean.TRUE.equals(modulo.getActivo()),
                isCoreModulo(modulo.getNombre()) ? "core" : "add-on",
                iconKeyFor(modulo.getNombre())
        );
    }

    private AdminPlanResponse toPlanResponse(PlanSaas plan) {
        return new AdminPlanResponse(
                plan.getId(),
                plan.getCodigo(),
                plan.getNombre(),
                plan.getDescripcion(),
                Boolean.TRUE.equals(plan.getActivo()),
                plan.getOrden(),
                plan.getMaxSucursales(),
                plan.getMaxUsuarios(),
                plan.getMaxOrdenesMes(),
                plan.getPrecioMensual(),
                plan.getPrecioAnual(),
                plan.getTrialDias(),
                plan.getFeatures()
        );
    }

    private AdminSuscripcionResponse toSuscripcionResponse(Taller taller, PlanSaas plan) {
        String planNormalizado = normalizePlanCodigo(plan != null ? plan.getCodigo() : null);
        int precioMensual = precioMensual(planNormalizado);
        boolean activo = Boolean.TRUE.equals(taller.getActivo());

        return new AdminSuscripcionResponse(
                taller.getId(),
                taller.getNombre(),
                planNormalizado,
                precioMensual,
                activo ? "activa" : "vencida",
                taller.getCreatedAt(),
                null,
                0,
                activo ? precioMensual : 0
        );
    }

    private String normalizePlanCodigo(String codigo) {
        String normalizado = normalize(codigo);
        return switch (normalizado) {
            case "basico" -> "starter";
            case "profesional" -> "pro";
            case "enterprise" -> "enterprise";
            default -> normalizado;
        };
    }

    private int precioMensual(String plan) {
        return switch (plan) {
            case "starter" -> 14990;
            case "pro" -> 29990;
            case "enterprise" -> 59990;
            default -> 0;
        };
    }

    private int calcularMrrTotal(List<Taller> talleres, Map<UUID, PlanSaas> planesById) {
        return talleres.stream()
                .filter(taller -> Boolean.TRUE.equals(taller.getActivo()))
                .map(taller -> planesById.get(taller.getPlanId()))
                .map(plan -> normalizePlanCodigo(plan != null ? plan.getCodigo() : null))
                .mapToInt(this::precioMensual)
                .sum();
    }

    private List<AdminPlanDistribucionResponse> distribucionPlanes(List<Taller> talleres, Map<UUID, PlanSaas> planesById) {
        Map<String, Integer> conteos = new LinkedHashMap<>();
        conteos.put("starter", 0);
        conteos.put("pro", 0);
        conteos.put("enterprise", 0);

        talleres.forEach(taller -> {
            PlanSaas plan = planesById.get(taller.getPlanId());
            String planNormalizado = normalizePlanCodigo(plan != null ? plan.getCodigo() : null);
            if (conteos.containsKey(planNormalizado)) {
                conteos.put(planNormalizado, conteos.get(planNormalizado) + 1);
            }
        });

        return conteos.entrySet().stream()
                .map(entry -> new AdminPlanDistribucionResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<AdminTallerConteoResponse> conteosPorTaller(List<Taller> talleres, TipoConteo tipo) {
        return talleres.stream()
                .map(taller -> new AdminTallerConteoResponse(taller.getNombre(), countFor(taller.getId(), tipo)))
                .toList();
    }

    private long countFor(UUID tallerId, TipoConteo tipo) {
        return switch (tipo) {
            case USUARIOS -> usuarioRepository.countByTallerId(tallerId);
            case CLIENTES -> clienteRepository.countByTallerId(tallerId);
            case SUCURSALES -> sucursalRepository.countByTallerId(tallerId);
            case ORDENES -> ordenRepository.countByTallerId(tallerId);
        };
    }

    private boolean isCoreModulo(String nombre) {
        if (nombre == null) {
            return false;
        }
        String normalizado = normalize(nombre);
        return normalizado.equals("dashboard")
                || normalizado.equals("ordenes")
                || normalizado.equals("clientes")
                || normalizado.equals("inventario");
    }

    private String iconKeyFor(String nombre) {
        return normalize(nombre).replace(" ", "-");
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    private OffsetDateTime inicioMesActual() {
        return YearMonth.now().atDay(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
    }

    private void validarCrearTaller(AdminCrearTallerRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload requerido");
        }
        if (isBlank(request.nombre())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre requerido");
        }
        if (isBlank(request.rut())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RUT requerido");
        }
        if (request.planId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan requerido");
        }
        tallerRepository.findByRut(request.rut().trim())
                .ifPresent(taller -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un taller con ese RUT");
                });
    }

    private void validarCrearPlan(AdminCrearPlanRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload requerido");
        }
        String codigo = normalizePlanCode(request.codigo());
        if (codigo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Codigo requerido");
        }
        if (isBlank(request.nombre())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre requerido");
        }
        requireMin(request.orden(), 0, "orden");
        requireMin(request.maxSucursales(), 1, "maxSucursales");
        requireMin(request.maxUsuarios(), 1, "maxUsuarios");
        requireOptionalMin(request.maxOrdenesMes(), 0, "maxOrdenesMes");
        requireMin(request.precioMensual(), BigDecimal.ZERO, "precioMensual");
        requireOptionalMin(request.precioAnual(), BigDecimal.ZERO, "precioAnual");
        requireMin(request.trialDias(), 0, "trialDias");
        normalizeFeatures(request.features());
    }

    private void requireMin(Integer value, int min, String field) {
        if (value == null || value < min) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " invalido");
        }
    }

    private void requireOptionalMin(Integer value, int min, String field) {
        if (value != null && value < min) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " invalido");
        }
    }

    private void requireMin(BigDecimal value, BigDecimal min, String field) {
        if (value == null || value.compareTo(min) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " invalido");
        }
    }

    private void requireOptionalMin(BigDecimal value, BigDecimal min, String field) {
        if (value != null && value.compareTo(min) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " invalido");
        }
    }

    private String normalizePlanCode(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    private String normalizeFeatures(Object features) {
        if (features == null) {
            return "{}";
        }
        if (features instanceof String text) {
            String trimmed = text.trim();
            if (trimmed.isEmpty()) {
                return "{}";
            }
            try {
                objectMapper.readTree(trimmed);
                return trimmed;
            } catch (JsonProcessingException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "features debe ser JSON valido");
            }
        }
        try {
            return objectMapper.writeValueAsString(features);
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "features debe ser JSON valido");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeOptional(String value) {
        return isBlank(value) ? null : value.trim();
    }

    public record AdminTallerResponse(
            UUID id,
            String nombre,
            String rut,
            String telefono,
            String email,
            String logoUrl,
            boolean activo,
            String estado,
            UUID planId,
            String plan,
            String planCodigo,
            String planNombre,
            OffsetDateTime fechaRegistro,
            OffsetDateTime fechaRenovacion,
            int cantidadUsuarios,
            int cantidadOTsMes,
            List<UUID> moduloIds,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            AdminTallerOperacionResponse operacion
    ) {
    }

    public record AdminTallerOperacionResponse(
            long sucursales,
            long usuarios,
            long clientes,
            long bicicletas,
            long servicios,
            long productos,
            long proveedores,
            long compras,
            long ordenes,
            long garantias,
            long membresias,
            long cobros,
            long notificacionesPendientes
    ) {
    }

    public record AdminModuloResponse(
            UUID id,
            String nombre,
            String descripcion,
            String ruta,
            boolean activo,
            String categoria,
            String iconKey
    ) {
    }

    public record AdminPlanResponse(
            UUID id,
            String codigo,
            String nombre,
            String descripcion,
            boolean activo,
            Integer orden,
            Integer maxSucursales,
            Integer maxUsuarios,
            Integer maxOrdenesMes,
            BigDecimal precioMensual,
            BigDecimal precioAnual,
            Integer trialDias,
            String features
    ) {
    }

    public record AdminCrearPlanRequest(
            String codigo,
            String nombre,
            String descripcion,
            Integer orden,
            Boolean activo,
            Integer maxSucursales,
            Integer maxUsuarios,
            Integer maxOrdenesMes,
            BigDecimal precioMensual,
            BigDecimal precioAnual,
            Integer trialDias,
            Object features
    ) {
    }

    public record AdminCrearTallerRequest(
            String nombre,
            String rut,
            String telefono,
            String email,
            UUID planId,
            boolean activo
    ) {
    }

    public record AdminActualizarEstadoTallerRequest(boolean activo) {
    }

    public record AdminSuscripcionResponse(
            UUID tallerId,
            String tallerNombre,
            String plan,
            int precioMensual,
            String estado,
            OffsetDateTime fechaInicio,
            OffsetDateTime fechaRenovacion,
            int diasRestantes,
            int mrr
    ) {
    }

    public record AdminSaasKpisResponse(
            int totalTalleres,
            long talleresActivos,
            long talleresNuevosMes,
            int mrrTotal,
            String mrrDelta,
            String churnRate,
            String trialToPaidRate
    ) {
    }

    public record AdminMetricasDetalleResponse(
            List<AdminMrrHistoricoResponse> mrrHistorico,
            List<AdminNuevosTalleresHistoricoResponse> nuevosTalleresHistorico,
            List<AdminChurnHistoricoResponse> churnHistorico,
            List<AdminPlanDistribucionResponse> distribucionPlanes,
            List<AdminTallerConteoResponse> usuariosPorTaller,
            List<AdminTallerConteoResponse> clientesPorTaller,
            List<AdminTallerConteoResponse> sucursalesPorTaller,
            List<AdminTallerConteoResponse> ordenesPorTaller
    ) {
    }

    public record AdminMrrHistoricoResponse(String mes, int mrr) {
    }

    public record AdminNuevosTalleresHistoricoResponse(String mes, int count) {
    }

    public record AdminChurnHistoricoResponse(String mes, double rate) {
    }

    public record AdminPlanDistribucionResponse(String plan, int count) {
    }

    public record AdminTallerConteoResponse(String tallerNombre, long count) {
    }

    private enum TipoConteo {
        USUARIOS,
        CLIENTES,
        SUCURSALES,
        ORDENES
    }
}
