package com.veloservice.administracion.infraestructure.persistence.repository;

import com.veloservice.administracion.domain.model.PlanSaas;
import com.veloservice.administracion.domain.model.Sucursal;
import com.veloservice.administracion.domain.model.Taller;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SucursalRepositoryTest {

    @Autowired private TestEntityManager em;
    @Autowired private SucursalRepository sucursalRepository;

    @Test
    void findByIdWithTallerEagerlyLoadsTaller() {
        OffsetDateTime now = OffsetDateTime.now();
        PlanSaas plan = em.persist(PlanSaas.builder()
                .codigo("test")
                .nombre("Test")
                .orden(0)
                .activo(true)
                .build());
        Taller taller = em.persist(Taller.builder()
                .planId(plan.getId())
                .nombre("Taller Central")
                .rut("12345678-9")
                .activo(true)
                .createdAt(now)
                .updatedAt(now)
                .build());
        Sucursal sucursal = em.persist(Sucursal.builder()
                .tallerId(taller.getId())
                .nombre("Sucursal Norte")
                .activo(true)
                .createdAt(now)
                .updatedAt(now)
                .build());
        em.flush();
        em.clear(); // detach so lazy loads would fail without join-fetch

        Optional<Sucursal> result = sucursalRepository.findByIdWithTaller(sucursal.getId());

        assertThat(result).isPresent();
        // accessing getTaller().getId() must NOT throw LazyInitializationException
        assertThat(result.get().getTaller().getId()).isEqualTo(taller.getId());
    }
}
