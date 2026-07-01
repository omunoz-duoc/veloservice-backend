package com.veloservice.ordenes.application.port;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioPort {
    Optional<UsuarioRef> findById(UUID id);
    // boolean existsMecanicoEnSucursal(UUID mecanicoId, UUID sucursalId);

    record UsuarioRef(UUID id, String nombre, String apellido) {}
}
