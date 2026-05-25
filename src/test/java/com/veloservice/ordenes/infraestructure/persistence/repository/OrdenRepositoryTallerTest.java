package com.veloservice.ordenes.infraestructure.persistence.repository;

import com.veloservice.administracion.domain.model.Sucursal;
import com.veloservice.administracion.domain.model.Taller;
import com.veloservice.ordenes.domain.model.Orden;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OrdenRepositoryTallerTest {

    @Autowired private TestEntityManager em;
    @Autowired private OrdenRepository ordenRepository;

    @Test
    void findAllByTallerIdOrderByFechaIngresoDesc_returnsOnlyOrdenesFromTaller() {
        // Talleres
        Taller t1 = em.persist(Taller.builder()
                .nombre("Taller Uno")
                .rut("11111111-1")
                .activo(true)
                .build());
        Taller t2 = em.persist(Taller.builder()
                .nombre("Taller Dos")
                .rut("22222222-2")
                .activo(true)
                .build());

        // Sucursales: S1 and S2 under T1, S3 under T2
        Sucursal s1 = em.persist(Sucursal.builder()
                .taller(t1)
                .nombre("Sucursal 1")
                .activo(true)
                .build());
        Sucursal s2 = em.persist(Sucursal.builder()
                .taller(t1)
                .nombre("Sucursal 2")
                .activo(true)
                .build());
        Sucursal s3 = em.persist(Sucursal.builder()
                .taller(t2)
                .nombre("Sucursal 3")
                .activo(true)
                .build());

        OffsetDateTime now = OffsetDateTime.now();

        // Ordenes
        Orden o1 = em.persist(Orden.builder()
                .externalId("EXT-001")
                .sucursalId(s1.getId())
                .bicicletaId(UUID.randomUUID())
                .numeroOrden("ORD-001")
                .descripcionTrabajo("Trabajo 1")
                .fechaIngreso(now.minusHours(2))
                .build());
        Orden o2 = em.persist(Orden.builder()
                .externalId("EXT-002")
                .sucursalId(s2.getId())
                .bicicletaId(UUID.randomUUID())
                .numeroOrden("ORD-002")
                .descripcionTrabajo("Trabajo 2")
                .fechaIngreso(now.minusHours(1))
                .build());
        Orden o3 = em.persist(Orden.builder()
                .externalId("EXT-003")
                .sucursalId(s3.getId())
                .bicicletaId(UUID.randomUUID())
                .numeroOrden("ORD-003")
                .descripcionTrabajo("Trabajo 3")
                .fechaIngreso(now)
                .build());

        em.flush();
        em.clear();

        List<Orden> result = ordenRepository.findAllByTallerIdOrderByFechaIngresoDesc(t1.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Orden::getId)
                .containsExactlyInAnyOrder(o1.getId(), o2.getId());
        assertThat(result).extracting(Orden::getId)
                .doesNotContain(o3.getId());
    }
}
