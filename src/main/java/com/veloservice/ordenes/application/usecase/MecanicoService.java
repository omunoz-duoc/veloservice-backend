package com.veloservice.ordenes.application.usecase;

import com.veloservice.administracion.domain.model.Usuario;
import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.security.SucursalContext;
import com.veloservice.config.tenant.TenantOperation;
import com.veloservice.ordenes.application.dto.MecanicoResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MecanicoService {

    private final UsuarioRepository usuarioRepository;

    @TenantOperation
    @Transactional(readOnly = true)
    public List<MecanicoResult> listar(Boolean activo) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            return List.of();
        }

        return usuarioRepository.findMecanicosBySucursalIdAndActivo(sucursalId, activo).stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    private MecanicoResult toResult(Usuario usuario) {
        String nombre = usuario.getNombre();
        String apellido = usuario.getApellido();
        return MecanicoResult.builder()
                .id(usuario.getId())
                .nombre(nombre)
                .apellido(apellido)
                .iniciales(buildInitials(nombre, apellido))
                .email(usuario.getEmail())
                .activo(usuario.getActivo())
                .sucursalId(usuario.getSucursal() != null ? usuario.getSucursal().getId() : null)
                .build();
    }

    private String buildInitials(String nombre, String apellido) {
        StringBuilder iniciales = new StringBuilder();
        appendFirstLetter(iniciales, nombre);
        appendFirstLetter(iniciales, apellido);
        return iniciales.toString();
    }

    private void appendFirstLetter(StringBuilder builder, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        String trimmed = value.trim();
        builder.append(Character.toUpperCase(trimmed.charAt(0)));
    }
}
