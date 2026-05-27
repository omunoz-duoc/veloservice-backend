package com.veloservice.ordenes.infraestructure.adapter;

import com.veloservice.auth.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.ordenes.application.port.UsuarioPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UsuarioAdapter implements UsuarioPort {

    private final UsuarioRepository usuarioRepository;

    @Override
    public Optional<UsuarioRef> findById(UUID id) {
        return usuarioRepository.findById(id)
                .map(u -> new UsuarioRef(u.getId(), u.getNombre(), u.getApellido()));
    }

    @Override
    public boolean existsMecanicoEnSucursal(UUID mecanicoId, UUID sucursalId) {
        return usuarioRepository.existsByIdAndSucursalIdAndRolNombreAndActivoTrue(
                mecanicoId, sucursalId, "MECANICO");
    }
}
