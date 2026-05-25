package com.veloservice.administracion.infraestructure.persistence.repository;

import com.veloservice.administracion.domain.model.Sucursal;
import com.veloservice.administracion.domain.model.Taller;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SucursalRepositoryTest {

    @Autowired private TestEntityManager em;
    @Autowired private SucursalRepository sucursalRepository;

    @Test
    void findByIdWithTallerEagerlyLoadsTaller() {
        Taller taller = em.persist(Taller.builder()
                .nombre("Taller Central")
                .rut("12345678-9")
                .activo(true)
                .build());
        Sucursal sucursal = em.persist(Sucursal.builder()
                .taller(taller)
                .nombre("Sucursal Norte")
                .activo(true)
                .build());
        em.flush();
        em.clear(); // detach so lazy loads would fail without join-fetch

        Optional<Sucursal> result = sucursalRepository.findByIdWithTaller(sucursal.getId());

        assertThat(result).isPresent();
        // accessing getTaller().getId() must NOT throw LazyInitializationException
        assertThat(result.get().getTaller().getId()).isEqualTo(taller.getId());
    }
}
