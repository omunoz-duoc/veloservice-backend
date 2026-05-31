package com.veloservice.administracion.infraestructure.adapter;

import com.veloservice.administracion.application.port.UsuarioPort;
import com.veloservice.auth.infraestructure.persistence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component("adminUsuarioAdapter")
@RequiredArgsConstructor
public class UsuarioAdapter implements UsuarioPort {

    private final UsuarioRepository usuarioRepository;

    // @Override
    // public List<UsuarioRef> findBySucursalIdAndActivo(UUID sucursalId) {
    //     return usuarioRepository.findBySucursalIdAndActivoTrue(sucursalId).stream()
    //             .map(u -> new UsuarioRef(u.getId(), u.getNombre(), u.getApellido()))
    //             .toList();
    // }

    // @Override
    // public Optional<UsuarioRef> findByIdAndSucursal(UUID id, UUID sucursalId) {
    //     return usuarioRepository.findById(id)
    //             .filter(u -> u.getSucursal() != null && sucursalId.equals(u.getSucursal().getId()))
    //             .map(u -> new UsuarioRef(u.getId(), u.getNombre(), u.getApellido()));
    // }

    @Override
    public void setActivo(UUID id, UUID sucursalId, boolean activo) {
        var usuario = usuarioRepository.findById(id)
                .filter(u -> u.getSucursal() != null && sucursalId.equals(u.getSucursal().getId()))
                .orElseThrow(() -> new IllegalArgumentException("Mecanico no encontrado"));
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
    }

    @Override
    public void setRol(UUID id, UUID sucursalId, String rolNombre) {
        var usuario = usuarioRepository.findById(id)
                .filter(u -> u.getSucursal() != null && sucursalId.equals(u.getSucursal().getId()))
                .orElseThrow(() -> new IllegalArgumentException("Mecanico no encontrado"));
        usuario.getRol().setNombre(rolNombre);
        usuarioRepository.save(usuario);
    }
}
