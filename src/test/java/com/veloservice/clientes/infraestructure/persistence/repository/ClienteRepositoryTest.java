package com.veloservice.clientes.infraestructure.persistence.repository;

import com.veloservice.clientes.domain.model.Cliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class ClienteRepositoryTest {

    @Autowired private ClienteRepository clienteRepository;

    @Test
    void saveAndFlushRejectsDuplicateRutWithinSameTaller() {
        UUID tallerId = UUID.randomUUID();
        String rut = "123456785";
        clienteRepository.saveAndFlush(cliente(tallerId, "CL-0001", rut));

        assertThatThrownBy(() -> clienteRepository.saveAndFlush(cliente(tallerId, "CL-0002", rut)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Cliente cliente(UUID tallerId, String codigoCliente, String rut) {
        OffsetDateTime now = OffsetDateTime.now();
        return Cliente.builder()
                .tallerId(tallerId)
                .codigoCliente(codigoCliente)
                .nombre("Ana")
                .apellido("Perez")
                .rut(rut)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
