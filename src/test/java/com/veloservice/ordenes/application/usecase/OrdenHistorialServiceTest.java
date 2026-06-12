package com.veloservice.ordenes.application.usecase;

import com.veloservice.config.tenant.UsuarioContext;
import com.veloservice.ordenes.domain.AccionHistorialEnum;
import com.veloservice.ordenes.domain.model.OrdenHistorial;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenHistorialRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrdenHistorialServiceTest {

    @Mock private OrdenHistorialRepository repository;

    @AfterEach
    void clearContext() {
        UsuarioContext.clear();
    }

    @Test
    void registrar_persistsEventWithActorAndSerializedDetalle() {
        UUID ordenId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID entidadId = UUID.randomUUID();
        UsuarioContext.setCurrentUser(usuarioId);

        OrdenHistorialService service = new OrdenHistorialService(repository, new com.fasterxml.jackson.databind.ObjectMapper());

        service.registrar(ordenId, AccionHistorialEnum.PRODUCTO_AGREGADO, "producto", entidadId,
                Map.of("nombre", "Cadena KMC", "cantidad", 2));

        ArgumentCaptor<OrdenHistorial> captor = ArgumentCaptor.forClass(OrdenHistorial.class);
        verify(repository).save(captor.capture());
        OrdenHistorial saved = captor.getValue();
        assertThat(saved.getOrdenId()).isEqualTo(ordenId);
        assertThat(saved.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(saved.getAccion()).isEqualTo("PRODUCTO_AGREGADO");
        assertThat(saved.getEntidad()).isEqualTo("producto");
        assertThat(saved.getEntidadId()).isEqualTo(entidadId);
        assertThat(saved.getDetalle()).contains("\"nombre\":\"Cadena KMC\"").contains("\"cantidad\":2");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void registrar_toleratesNullActorAndNullDetalle() {
        UUID ordenId = UUID.randomUUID();
        OrdenHistorialService service = new OrdenHistorialService(repository, new com.fasterxml.jackson.databind.ObjectMapper());

        service.registrar(ordenId, AccionHistorialEnum.ESTADO_CAMBIADO, null, null, null);

        ArgumentCaptor<OrdenHistorial> captor = ArgumentCaptor.forClass(OrdenHistorial.class);
        verify(repository).save(captor.capture());
        OrdenHistorial saved = captor.getValue();
        assertThat(saved.getUsuarioId()).isNull();
        assertThat(saved.getDetalle()).isNull();
    }

    @Test
    void registrar_emptyDetalle_storesNull() {
        OrdenHistorialService service = new OrdenHistorialService(repository, new com.fasterxml.jackson.databind.ObjectMapper());

        service.registrar(UUID.randomUUID(), AccionHistorialEnum.ESTADO_CAMBIADO, null, null, Map.of());

        ArgumentCaptor<OrdenHistorial> captor = ArgumentCaptor.forClass(OrdenHistorial.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getDetalle()).isNull();
    }
}
