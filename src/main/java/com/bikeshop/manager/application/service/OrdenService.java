package com.bikeshop.manager.application.service;

import com.bikeshop.manager.application.dto.EstadoChangeRequest;
import com.bikeshop.manager.application.dto.MultimediaRequest;
import com.bikeshop.manager.application.dto.OrdenRequest;
import com.bikeshop.manager.application.dto.OrdenResponse;
import com.bikeshop.manager.domain.tenant.Multimedia;
import com.bikeshop.manager.domain.tenant.Orden;
import com.bikeshop.manager.domain.tenant.OrdenEstado;
import com.bikeshop.manager.infrastructure.persistence.repository.MultimediaRepository;
import com.bikeshop.manager.infrastructure.persistence.repository.OrdenEstadoRepository;
import com.bikeshop.manager.infrastructure.persistence.repository.OrdenRepository;
import com.bikeshop.manager.infrastructure.rls.TenantOperation;
import com.bikeshop.manager.infrastructure.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles work order lifecycle operations.
 */
@Service
@RequiredArgsConstructor
public class OrdenService {

    private static final List<String> ESTADOS_VALIDOS = List.of(
            "recibida", "en_diagnostico", "esperando_repuestos", "en_reparacion",
            "control_calidad", "lista_para_entrega", "entregada", "cancelada"
    );

    private static final List<String> TRANSICIONES_VALIDAS = List.of(
            "recibida->en_diagnostico",
            "en_diagnostico->esperando_repuestos",
            "en_diagnostico->en_reparacion",
            "esperando_repuestos->en_reparacion",
            "en_reparacion->control_calidad",
            "control_calidad->lista_para_entrega",
            "lista_para_entrega->entregada",
            "recibida->cancelada",
            "en_diagnostico->cancelada"
    );

    private final OrdenRepository ordenRepository;
    private final OrdenEstadoRepository ordenEstadoRepository;
    private final MultimediaRepository multimediaRepository;
    private final SecuenciaService secuenciaService;

    /**
     * Creates a work order with mandatory multimedia evidence.
     *
     * @param request work order request
     * @return created work order response
     */
    @TenantOperation
    @Transactional
    public OrdenResponse crear(OrdenRequest request) {
        UUID sucursalId = TenantContext.getCurrentTenant();
        UUID usuarioId = TenantContext.getCurrentUser();
        if (sucursalId == null || usuarioId == null) {
            throw new IllegalStateException("Contexto de sucursal/usuario requerido");
        }

        if (request.getMultimedia() == null || request.getMultimedia().isEmpty()) {
            throw new IllegalArgumentException("Evidencia multimedia obligatoria (RN01)");
        }

        String numeroOrden = secuenciaService.generarNumeroOrden(sucursalId);

        Orden orden = Orden.builder()
            .sucursalId(sucursalId)
                .bicicletaId(request.getBicicletaId())
                .mecanicoId(usuarioId)
                .numeroOrden(numeroOrden)
            .estado("recibida")
                .tipo(request.getTipo())
                .diagnosticoInicial(request.getDiagnosticoInicial())
                .observacionesCliente(request.getObservacionesCliente())
                .descuentoManual(BigDecimal.ZERO)
                .porcentajeDescuentoMembresia(BigDecimal.ZERO)
                .fechaIngreso(LocalDateTime.now())
                .build();

        orden = ordenRepository.save(orden);

        for (MultimediaRequest m : request.getMultimedia()) {
            Multimedia multimedia = Multimedia.builder()
                    .ordenId(orden.getId())
                    .usuarioId(usuarioId)
                    .url(m.getUrl())
                    .tipoArchivo(m.getTipoArchivo())
                    .etapa("ingreso")
                    .descripcion(m.getDescripcion())
                    .build();
            multimediaRepository.save(multimedia);
        }

        registrarEstado(orden.getId(), sucursalId, usuarioId, "N/A", "recibida", "Creacion de orden de trabajo");

        return toResponse(orden);
    }

    /**
     * Changes the state of a work order and records audit evidence.
     *
     * @param ordenId work order identifier
     * @param request change request
     * @return updated work order response
     */
    @TenantOperation
    @Transactional
    public OrdenResponse cambiarEstado(UUID ordenId, EstadoChangeRequest request) {
        UUID sucursalId = TenantContext.getCurrentTenant();
        UUID usuarioId = TenantContext.getCurrentUser();

        Orden orden = ordenRepository.findByIdAndSucursalId(ordenId, sucursalId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        String estadoActual = orden.getEstado();
        String estadoNuevo = request.getNuevoEstado();

        if (!ESTADOS_VALIDOS.contains(estadoNuevo)) {
            throw new IllegalArgumentException("Estado no valido: " + estadoNuevo);
        }

        String transicion = estadoActual + "->" + estadoNuevo;
        if (!TRANSICIONES_VALIDAS.contains(transicion)) {
            throw new IllegalArgumentException("Transicion no permitida: " + transicion);
        }

        if ("lista_para_entrega".equals(estadoNuevo)) {
            boolean tieneEvidenciaTecnica = multimediaRepository.existsByOrdenIdAndEtapa(ordenId, "reparacion");
            if (!tieneEvidenciaTecnica) {
                throw new IllegalArgumentException(
                        "No se puede cambiar a lista_para_entrega sin evidencia tecnica final (RN02)");
            }
        }

        if ("cancelada".equals(estadoNuevo) && List.of("lista_para_entrega", "entregada").contains(estadoActual)) {
            throw new IllegalArgumentException("No se puede cancelar una orden en estado " + estadoActual);
        }

        orden.setEstado(estadoNuevo);
        if ("entregada".equals(estadoNuevo)) {
            orden.setFechaEntrega(LocalDateTime.now());
        }
        orden = ordenRepository.save(orden);

        registrarEstado(ordenId, sucursalId, usuarioId, estadoActual, estadoNuevo, request.getObservacion());

        return toResponse(orden);
    }

    /**
     * Lists work orders for the current tenant.
     *
     * @return work orders
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public List<OrdenResponse> listar() {
        UUID sucursalId = TenantContext.getCurrentTenant();
        return ordenRepository.findAllBySucursalIdOrderByFechaIngresoDesc(sucursalId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a work order by identifier.
     *
     * @param id work order identifier
     * @return work order response
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public OrdenResponse obtener(UUID id) {
        UUID sucursalId = TenantContext.getCurrentTenant();
        Orden orden = ordenRepository.findByIdAndSucursalId(id, sucursalId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
        return toResponse(orden);
    }

        private void registrarEstado(UUID ordenId, UUID sucursalId, UUID usuarioId,
                                 String anterior, String nuevo, String observacion) {
        OrdenEstado auditoria = OrdenEstado.builder()
                .ordenId(ordenId)
                .usuarioId(usuarioId)
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .observacion(observacion)
                .build();
        ordenEstadoRepository.save(auditoria);
    }

    private OrdenResponse toResponse(Orden orden) {
        return OrdenResponse.builder()
                .id(orden.getId())
                .numeroOrden(orden.getNumeroOrden())
                .estado(orden.getEstado())
                .tipo(orden.getTipo())
                .bicicletaId(orden.getBicicletaId())
                .mecanicoId(orden.getMecanicoId())
                .diagnosticoInicial(orden.getDiagnosticoInicial())
                .fechaIngreso(orden.getFechaIngreso())
                .fechaPrometida(orden.getFechaPrometida())
                .build();
    }
}
