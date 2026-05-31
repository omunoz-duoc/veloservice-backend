package com.veloservice.administracion.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioPort {
    // List<UsuarioRef> findBySucursalIdAndActivo(UUID sucursalId);
    // Optional<UsuarioRef> findByIdAndSucursal(UUID id, UUID sucursalId);
    void setActivo(UUID id, UUID sucursalId, boolean activo);
    void setRol(UUID id, UUID sucursalId, String rolNombre);

    record UsuarioRef(UUID id, String nombre, String apellido) {}
}
