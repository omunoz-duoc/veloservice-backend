package com.veloservice.auth.application.usecase;

import com.veloservice.auth.application.dto.MecanicoResult;
import com.veloservice.auth.domain.model.Usuario;
import com.veloservice.auth.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.TallerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MecanicoService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<MecanicoResult> listarActivos() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId != null) {
            return usuarioRepository.findActiveMecanicosBySucursalId(sucursalId).stream()
                    .map(this::toResult)
                    .toList();
        }

        UUID tallerId = TallerContext.getCurrentTaller();
        if (tallerId != null) {
            return usuarioRepository.findActiveMecanicosByTallerId(tallerId).stream()
                    .map(this::toResult)
                    .toList();
        }

        return List.of();
    }

    private MecanicoResult toResult(Usuario usuario) {
        return new MecanicoResult(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getRol().getNombre()
        );
    }
}
