package com.veloservice.administracion.interfaces.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.Normalizer;
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
