package com.veloservice.auth.application.port;

import java.util.Optional;
import java.util.UUID;

public interface SucursalPort {
    Optional<SucursalRef> findById(UUID id);
    Optional<UUID> findTallerIdBySucursalId(UUID id);

    record SucursalRef(UUID id) {}
}
