package com.veloservice.administracion.application.usecase;

import com.veloservice.administracion.interfaces.rest.MecanicoResponse;
import com.veloservice.auth.domain.model.Usuario;
import com.veloservice.auth.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.administracion.interfaces.rest.MecanicoPerfilResponse;
import java.time.Duration;
import com.veloservice.ordenes.domain.EstadoOrdenEnum;
import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.TenantOperation;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles mechanic operations.
 */
@Service
@RequiredArgsConstructor
public class MecanicoService {

    private final UsuarioRepository usuarioRepository;
    private final OrdenRepository ordenRepository;

    private static final List<EstadoOrdenEnum> ESTADOS_FINALES = List.of(
            EstadoOrdenEnum.entregada
    );

    /**
     * Lists active mechanics with their current orders.
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public List<MecanicoResponse> listarActivos() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        return usuarioRepository.findBySucursalIdAndActivoTrue(sucursalId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Changes mechanic active status.
     */
    @TenantOperation
    @Transactional
    public void cambiarEstado(UUID id, boolean activo) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        Usuario usuario = usuarioRepository.findById(id)
                .filter(u -> u.getSucursal().getId().equals(sucursalId))
                .orElseThrow(() -> new IllegalArgumentException("Mecanico no encontrado"));
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
    }

    /**
     * Changes mechanic role.
     */
    @TenantOperation
    @Transactional
    public void cambiarRol(UUID id, String nuevoRol) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        Usuario usuario = usuarioRepository.findById(id)
                .filter(u -> u.getSucursal().getId().equals(sucursalId))
                .orElseThrow(() -> new IllegalArgumentException("Mecanico no encontrado"));
        usuario.getRol().setNombre(nuevoRol);
        usuarioRepository.save(usuario);
    }

    private MecanicoResponse toResponse(Usuario usuario) {
        List<MecanicoResponse.OrdenEnCursoResponse> ordenesEnCurso =
                ordenRepository.findByMecanicoIdAndEstadoNotIn(usuario.getId(), ESTADOS_FINALES)
                        .stream()
                        .map(o -> MecanicoResponse.OrdenEnCursoResponse.builder()
                                .id(o.getNumeroOrden())
                                .build())
                        .collect(Collectors.toList());

        return MecanicoResponse.builder()
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .ordenesEnCurso(ordenesEnCurso)
                .build();
    }

    /**
     * Obtiene el perfil técnico del mecánico: métricas, órdenes completadas y datos relevantes
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public MecanicoPerfilResponse obtenerPerfilTecnico(UUID mecanicoId) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        Usuario usuario = usuarioRepository.findById(mecanicoId)
                .filter(u -> u.getSucursal().getId().equals(sucursalId))
                .orElseThrow(() -> new IllegalArgumentException("Mecánico no encontrado"));

        var ordenes = ordenRepository.findAllBySucursalIdAndMecanicoIdOrderByFechaIngresoDesc(sucursalId, mecanicoId);
        int totalOrdenes = ordenes.size();
        var completadas = ordenes.stream().filter(o -> o.getEstado() == EstadoOrdenEnum.entregada).collect(Collectors.toList());
        int ordenesCompletadas = completadas.size();
        double tiempoPromedioDias = completadas.stream()
                .filter(o -> o.getFechaIngreso() != null && o.getFechaEntrega() != null)
                .mapToLong(o -> Duration.between(o.getFechaIngreso(), o.getFechaEntrega()).toDays())
                .average().orElse(0);
        var ordenesRecientes = ordenes.stream().limit(5)
                .map(o -> MecanicoPerfilResponse.OrdenResumenDTO.builder()
                        .id(o.getId())
                        .descripcion(o.getDescripcionTrabajo())
                        .estado(o.getEstado() != null ? o.getEstado().name() : null)
                        .cliente("") // Completar con nombre cliente si es necesario
                        .build())
                .collect(Collectors.toList());
        String nivelTecnico = ordenesCompletadas > 20 ? "Experto" : (ordenesCompletadas > 5 ? "Intermedio" : "Inicial");

        return MecanicoPerfilResponse.builder()
                .mecanicoId(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .totalOrdenes(totalOrdenes)
                .ordenesCompletadas(ordenesCompletadas)
                .tiempoPromedioDias(tiempoPromedioDias)
                .ordenesRecientes(ordenesRecientes)
                .nivelTecnico(nivelTecnico)
                .build();
    }
}
