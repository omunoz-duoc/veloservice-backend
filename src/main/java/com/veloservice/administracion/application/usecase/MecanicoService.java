package com.veloservice.administracion.application.usecase;

import com.veloservice.administracion.domain.model.Usuario;
import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.administracion.interfaces.rest.MecanicoResponse;
import com.veloservice.config.enums.EstadoOrdenEnum;
import com.veloservice.config.security.SucursalContext;
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
            EstadoOrdenEnum.entregada,
            EstadoOrdenEnum.cancelada
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
}
