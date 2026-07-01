package com.veloservice.clientes.interfaces.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.veloservice.clientes.application.dto.BicicletaDetalleResult;
import com.veloservice.clientes.application.dto.ClienteDetalleResult;
import com.veloservice.clientes.application.dto.OrdenResumenResult;
import com.veloservice.clientes.interfaces.rest.dto.BicicletaDetalleItem;
import com.veloservice.clientes.interfaces.rest.dto.ClienteDetalleResponse;
import com.veloservice.clientes.interfaces.rest.dto.OrdenResumenItem;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ClienteMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void toDetalleResponseMapsAllFieldsAndConcatenatesNombre() {
        UUID bikeId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        ClienteDetalleResult result = ClienteDetalleResult.builder()
                .id(UUID.randomUUID())
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan@example.com")
                .telefono("+56912345678")
                .direccion("Av. Siempre Viva 123")
                .rut("12345678-9")
                .clienteDesde(now)
                .bicicletasCount(1)
                .bicicletas(List.of(new BicicletaDetalleResult(
                        bikeId, "Trek", "Marlin 5", "MTB", "29", "Rojo", "ABC123", 2023, "Notas")))
                .otsCount(3L)
                .lastOts(List.of(new OrdenResumenResult(
                        "OT-0001", "Mantención", "En proceso", now.minusDays(1))))
                .build();

        ClienteDetalleResponse response = ClienteMapper.toDetalleResponse(result);

        assertThat(response.getNombre()).isEqualTo("Juan Pérez");
        assertThat(response.getEmail()).isEqualTo("juan@example.com");
        assertThat(response.getTelefono()).isEqualTo("+56912345678");
        assertThat(response.getDireccion()).isEqualTo("Av. Siempre Viva 123");
        assertThat(response.getRut()).isEqualTo("12345678-9");
        assertThat(response.getClienteDesde()).isEqualTo(now);
        assertThat(response.getBicicletasCount()).isEqualTo(1);
        assertThat(response.getOtsCount()).isEqualTo(3L);

        assertThat(response.getBicicletas()).hasSize(1);
        BicicletaDetalleItem bike = response.getBicicletas().get(0);
        assertThat(bike.id()).isEqualTo(bikeId);
        assertThat(bike.marca()).isEqualTo("Trek");
        assertThat(bike.modelo()).isEqualTo("Marlin 5");
        assertThat(bike.tipo()).isEqualTo("MTB");
        assertThat(bike.aro()).isEqualTo("29");
        assertThat(bike.color()).isEqualTo("Rojo");
        assertThat(bike.numeroSerie()).isEqualTo("ABC123");
        assertThat(bike.anio()).isEqualTo(2023);
        assertThat(bike.notas()).isEqualTo("Notas");

        assertThat(response.getLastOts()).hasSize(1);
        OrdenResumenItem ot = response.getLastOts().get(0);
        assertThat(ot.numeroOrden()).isEqualTo("OT-0001");
        assertThat(ot.tipoOrden()).isEqualTo("Mantención");
        assertThat(ot.estadoOrden()).isEqualTo("En proceso");
        assertThat(ot.fechaIngreso()).isEqualTo(now.minusDays(1));
    }

    @Test
    void toDetalleResponseHandlesNullApellido() {
        ClienteDetalleResult result = ClienteDetalleResult.builder()
                .id(UUID.randomUUID())
                .nombre("Solo")
                .apellido(null)
                .email("solo@example.com")
                .clienteDesde(OffsetDateTime.now())
                .bicicletasCount(0)
                .bicicletas(List.of())
                .otsCount(0L)
                .lastOts(List.of())
                .build();

        ClienteDetalleResponse response = ClienteMapper.toDetalleResponse(result);
        assertThat(response.getNombre()).isEqualTo("Solo");
    }

    @Test
    void toDetalleResponseProducesExpectedJsonKeys() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-01-15T10:30:00-03:00");
        ClienteDetalleResult result = ClienteDetalleResult.builder()
                .id(UUID.randomUUID())
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan@example.com")
                .telefono("+56912345678")
                .direccion("Av. Siempre Viva 123")
                .rut("12345678-9")
                .clienteDesde(now)
                .bicicletasCount(0)
                .bicicletas(List.of())
                .otsCount(0L)
                .lastOts(List.of())
                .build();

        ClienteDetalleResponse response = ClienteMapper.toDetalleResponse(result);
        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"nombre\"");
        assertThat(json).contains("\"email\"");
        assertThat(json).contains("\"telefono\"");
        assertThat(json).contains("\"direccion\"");
        assertThat(json).contains("\"rut\"");
        assertThat(json).contains("\"clienteDesde\"");
        assertThat(json).contains("\"bicicletasCount\"");
        assertThat(json).contains("\"bicicletas\"");
        assertThat(json).contains("\"otsCount\"");
        assertThat(json).contains("\"lastOts\"");
    }
}
