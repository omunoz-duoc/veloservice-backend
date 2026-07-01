package com.veloservice.ordenes.application.port;

import java.util.Optional;
import java.util.UUID;

public interface BicicletaPort {
    Optional<BicicletaRef> findById(UUID id);

    record ClienteRef(String nombre, String apellido, String telefono) {}

    record BicicletaRef(
        UUID id,
        String marca,
        String modelo,
        String tipo,
        String color,
        String aro,
        ClienteRef cliente
    ) {}
}
