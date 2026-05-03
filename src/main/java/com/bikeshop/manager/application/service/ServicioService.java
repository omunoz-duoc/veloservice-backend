package com.bikeshop.manager.application.service;

import com.bikeshop.manager.application.dto.ServicioRequest;
import com.bikeshop.manager.application.dto.ServicioResponse;
import com.bikeshop.manager.application.dto.SucursalServicioRequest;
import com.bikeshop.manager.domain.platform.Servicio;
import com.bikeshop.manager.domain.tenant.SucursalServicio;
import com.bikeshop.manager.infrastructure.persistence.repository.ServicioRepository;
import com.bikeshop.manager.infrastructure.persistence.repository.SucursalServicioRepository;
import com.bikeshop.manager.infrastructure.security.SucursalContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final SucursalServicioRepository sucursalServicioRepository;

    @Transactional(readOnly = true)
    public List<ServicioResponse> listar() {
        return servicioRepository.findAll().stream()
                .map(s -> ServicioResponse.builder()
                        .id(s.getId())
                        .nombre(s.getNombre())
                        .descripcion(s.getDescripcion())
                        .precioBase(s.getPrecioBase())
                        .activo(s.getActivo())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public ServicioResponse crear(ServicioRequest request) {
        Servicio s = Servicio.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precioBase(request.getPrecioBase())
                .activo(request.getActivo() == null ? true : request.getActivo())
                .build();
        s = servicioRepository.save(s);
        return ServicioResponse.builder()
                .id(s.getId())
                .nombre(s.getNombre())
                .descripcion(s.getDescripcion())
                .precioBase(s.getPrecioBase())
                .activo(s.getActivo())
                .build();
    }

    @Transactional
    public ServicioResponse actualizar(UUID id, ServicioRequest request) {
        Servicio s = servicioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
        s.setNombre(request.getNombre());
        s.setDescripcion(request.getDescripcion());
        s.setPrecioBase(request.getPrecioBase());
        s.setActivo(request.getActivo() == null ? s.getActivo() : request.getActivo());
        s = servicioRepository.save(s);
        return ServicioResponse.builder()
                .id(s.getId())
                .nombre(s.getNombre())
                .descripcion(s.getDescripcion())
                .precioBase(s.getPrecioBase())
                .activo(s.getActivo())
                .build();
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!servicioRepository.existsById(id)) throw new IllegalArgumentException("Servicio no encontrado");
        servicioRepository.deleteById(id);
    }

    @Transactional
    public void asignarPrecioSucursal(SucursalServicioRequest request) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) throw new IllegalStateException("Sucursal no presente en contexto");

        SucursalServicio link = sucursalServicioRepository.findBySucursalIdAndServicioId(sucursalId, request.getServicioId())
                .orElseGet(() -> SucursalServicio.builder()
                        .sucursalId(sucursalId)
                        .servicioId(request.getServicioId())
                        .build());
        link.setPrecioPersonalizado(request.getPrecioPersonalizado());
        link.setActivo(true);
        sucursalServicioRepository.save(link);
    }

    @Transactional(readOnly = true)
    public List<SucursalServicio> listarServiciosSucursal() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) throw new IllegalStateException("Sucursal no presente en contexto");
        return sucursalServicioRepository.findAllBySucursalIdAndActivoTrue(sucursalId);
    }
}
