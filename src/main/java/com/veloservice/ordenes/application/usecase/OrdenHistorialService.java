package com.veloservice.ordenes.application.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veloservice.config.tenant.UsuarioContext;
import com.veloservice.ordenes.domain.AccionHistorialEnum;
import com.veloservice.ordenes.domain.model.OrdenHistorial;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenHistorialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Punto único de escritura del audit trail de órdenes (orden_historial).
 * Se invoca dentro de la misma transacción que la mutación; si la mutación falla, el registro se revierte con ella.
 */
@Service
public class OrdenHistorialService {

    private static final Logger log = LoggerFactory.getLogger(OrdenHistorialService.class);

    private final OrdenHistorialRepository repository;
    private final ObjectMapper mapper;

    public OrdenHistorialService(OrdenHistorialRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Registra un evento de historial para una orden.
     */
    public void registrar(UUID ordenId,
                          AccionHistorialEnum accion,
                          String entidad,
                          UUID entidadId,
                          Map<String, Object> detalle) {
        repository.save(OrdenHistorial.builder()
                .ordenId(ordenId)
                .usuarioId(UsuarioContext.getCurrentUser())
                .accion(accion.name())
                .entidad(entidad)
                .entidadId(entidadId)
                .detalle(serializar(detalle))
                .createdAt(OffsetDateTime.now())
                .build());
    }

    private String serializar(Map<String, Object> detalle) {
        if (detalle == null || detalle.isEmpty()) {
            return null;
        }
        try {
            return mapper.writeValueAsString(detalle);
        } catch (Exception ex) {
            // El historial nunca debe romper la operación de negocio.
            log.warn("orden_historial: no se pudo serializar detalle para accion, se omite el campo", ex);
            return null;
        }
    }
}
