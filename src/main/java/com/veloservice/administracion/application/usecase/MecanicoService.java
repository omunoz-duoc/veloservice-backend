package com.veloservice.administracion.application.usecase;

import com.veloservice.administracion.application.dto.MecanicoDisponibleResult;
import com.veloservice.administracion.domain.model.Usuario;
import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.security.SucursalContext;
import com.veloservice.config.tenant.TenantOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles mechanic listings for the current tenant.
 */
@Service
@RequiredArgsConstructor
public class MecanicoService {
    private static final String ROL_MECANICO = "MECANICO";

    private final UsuarioRepository usuarioRepository;

    /**
     * Lists active mechanics for the current branch.
     *
     * @return available mechanics
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public List<MecanicoDisponibleResult> listarDisponibles() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            return List.of();
        }
        return usuarioRepository.findBySucursalIdAndRolNombreAndActivoTrue(sucursalId, ROL_MECANICO).stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    private MecanicoDisponibleResult toResult(Usuario usuario) {
        return MecanicoDisponibleResult.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .iniciales(buildIniciales(usuario.getNombre(), usuario.getApellido()))
                .build();
    }

    private String buildIniciales(String nombre, String apellido) {
        StringBuilder iniciales = new StringBuilder();
        if (nombre != null && !nombre.isBlank()) {
            iniciales.append(Character.toUpperCase(nombre.trim().charAt(0)));
        }
        if (apellido != null && !apellido.isBlank()) {
            iniciales.append(Character.toUpperCase(apellido.trim().charAt(0)));
        }
        return iniciales.toString();
    }
}
