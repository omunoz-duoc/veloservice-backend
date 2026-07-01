package com.veloservice.auth.infraestructure.adapter;

import com.veloservice.administracion.infraestructure.persistence.repository.SucursalRepository;
import com.veloservice.auth.application.port.SucursalPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SucursalAdapter implements SucursalPort {

    private final SucursalRepository sucursalRepository;

    @Override
    public Optional<SucursalRef> findById(UUID id) {
        return sucursalRepository.findById(id)
                .map(s -> new SucursalRef(s.getId()));
    }

    @Override
    public Optional<UUID> findTallerIdBySucursalId(UUID id) {
        return sucursalRepository.findById(id)
                .map(s -> s.getTallerId());
    }
}
