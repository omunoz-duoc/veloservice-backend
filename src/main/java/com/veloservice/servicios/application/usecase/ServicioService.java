package com.veloservice.servicios.application.usecase;

import com.veloservice.servicios.application.dto.ServicioCreateCommand;
import com.veloservice.servicios.application.dto.ServicioResult;
import com.veloservice.servicios.application.dto.SucursalServicioPrecioCommand;
import com.veloservice.servicios.application.dto.SucursalServicioResult;
import com.veloservice.servicios.domain.model.Servicio;
import com.veloservice.servicios.domain.model.SucursalServicio;
import com.veloservice.servicios.infraestructure.persistence.repository.ServicioRepository;
import com.veloservice.servicios.infraestructure.persistence.repository.SucursalServicioRepository;
import com.veloservice.config.tenant.SucursalContext;
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
        public List<ServicioResult> listar() {
        return servicioRepository.findAll().stream()
            .map(this::toResult)
            .collect(Collectors.toList());
    }

    @Transactional
        public ServicioResult crear(ServicioCreateCommand command) {
        Servicio s = Servicio.builder()
            .nombre(command.getNombre())
            .descripcion(command.getDescripcion())
            .precioBase(command.getPrecioBase())
            .activo(command.getActivo() == null ? true : command.getActivo())
                .build();
        s = servicioRepository.save(s);
        return toResult(s);
    }

    @Transactional
    public ServicioResult actualizar(UUID id, ServicioCreateCommand command) {
        Servicio s = servicioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
        s.setNombre(command.getNombre());
        s.setDescripcion(command.getDescripcion());
        s.setPrecioBase(command.getPrecioBase());
        s.setActivo(command.getActivo() == null ? s.getActivo() : command.getActivo());
        s = servicioRepository.save(s);
        return toResult(s);
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!servicioRepository.existsById(id)) throw new IllegalArgumentException("Servicio no encontrado");
        servicioRepository.deleteById(id);
    }

    @Transactional
    public void asignarPrecioSucursal(SucursalServicioPrecioCommand command) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) throw new IllegalStateException("Sucursal no presente en contexto");

        SucursalServicio link = sucursalServicioRepository.findBySucursalIdAndServicioId(sucursalId, command.getServicioId())
                .orElseGet(() -> SucursalServicio.builder()
                        .sucursalId(sucursalId)
                        .servicioId(command.getServicioId())
                        .build());
        link.setPrecioPersonalizado(command.getPrecioPersonalizado());
        link.setActivo(true);
        sucursalServicioRepository.save(link);
    }

    @Transactional(readOnly = true)
    public List<SucursalServicioResult> listarServiciosSucursal() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) throw new IllegalStateException("Sucursal no presente en contexto");
        return sucursalServicioRepository.findAllBySucursalIdAndActivoTrue(sucursalId).stream()
                .map(this::toSucursalResult)
                .collect(Collectors.toList());
    }

    private ServicioResult toResult(Servicio servicio) {
        return ServicioResult.builder()
                .id(servicio.getId())
                .nombre(servicio.getNombre())
                .descripcion(servicio.getDescripcion())
                .precioBase(servicio.getPrecioBase())
                .activo(servicio.getActivo())
                .build();
    }

    private SucursalServicioResult toSucursalResult(SucursalServicio link) {
        return SucursalServicioResult.builder()
                .id(link.getId())
                .sucursalId(link.getSucursalId())
                .servicioId(link.getServicioId())
                .precioPersonalizado(link.getPrecioPersonalizado())
                .activo(link.getActivo())
                .createdAt(link.getCreatedAt())
                .build();
    }
}