package com.veloservice.administracion.interfaces.rest;

import com.veloservice.administracion.domain.model.Modulo;
import com.veloservice.administracion.domain.model.PlanSaas;
import com.veloservice.administracion.domain.model.Taller;
import com.veloservice.administracion.infraestructure.persistence.repository.ModuloRepository;
import com.veloservice.administracion.infraestructure.persistence.repository.PlanSaasRepository;
import com.veloservice.administracion.infraestructure.persistence.repository.TallerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.time.YearMonth;
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

    @GetMapping("/talleres")
    public List<AdminTallerResponse> listarTalleres() {
        Map<UUID, PlanSaas> planesById = planSaasRepository.findAll().stream()
                .collect(Collectors.toMap(PlanSaas::getId, Function.identity()));

        return tallerRepository.findAll().stream()
                .map(taller -> toTallerResponse(taller, planesById.get(taller.getPlanId())))
                .toList();
    }

    @GetMapping("/talleres/{id}")
    public AdminTallerResponse obtenerTaller(@PathVariable UUID id) {
        Taller taller = tallerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Taller no encontrado"));
        PlanSaas plan = taller.getPlanId() != null
                ? planSaasRepository.findById(taller.getPlanId()).orElse(null)
                : null;
        return toTallerResponse(taller, plan);
    }

    @GetMapping("/modulos")
    public List<AdminModuloResponse> listarModulos() {
        return moduloRepository.findAllByActivoTrueOrderByNombreAsc().stream()
                .map(this::toModuloResponse)
                .toList();
    }

    @GetMapping("/metrics/saas-kpis")
    public AdminSaasKpisResponse obtenerSaasKpis() {
        List<Taller> talleres = tallerRepository.findAll();
        OffsetDateTime inicioMes = YearMonth.now().atDay(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset());

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
                0,
                "N/A",
                "N/A",
                "N/A"
        );
    }

    private AdminTallerResponse toTallerResponse(Taller taller, PlanSaas plan) {
        boolean activo = Boolean.TRUE.equals(taller.getActivo());
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
                plan != null ? plan.getCodigo() : null,
                plan != null ? plan.getNombre() : null,
                taller.getCreatedAt(),
                null,
                0,
                0,
                List.of()
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
            String planCodigo,
            String planNombre,
            OffsetDateTime fechaRegistro,
            OffsetDateTime fechaRenovacion,
            int cantidadUsuarios,
            int cantidadOTsMes,
            List<UUID> moduloIds
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
}
