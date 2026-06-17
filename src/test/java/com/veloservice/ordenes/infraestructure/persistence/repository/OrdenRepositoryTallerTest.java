package com.veloservice.ordenes.infraestructure.persistence.repository;

import com.veloservice.administracion.domain.model.PlanSaas;
import com.veloservice.administracion.domain.model.Sucursal;
import com.veloservice.administracion.domain.model.Taller;
import com.veloservice.clientes.domain.model.Bicicleta;
import com.veloservice.clientes.domain.model.Cliente;
import com.veloservice.ordenes.application.dto.OrdenResumenClienteResult;
import com.veloservice.ordenes.domain.model.EstadoOrden;
import com.veloservice.ordenes.domain.model.Orden;
import com.veloservice.ordenes.domain.model.TipoOrden;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Pageable;
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
        OffsetDateTime now = OffsetDateTime.now();
        PlanSaas plan = em.persist(PlanSaas.builder()
                .codigo("test")
                .nombre("Test")
                .orden(0)
                .activo(true)
                .build());
        Taller t1 = em.persist(Taller.builder()
                .planId(plan.getId())
                .nombre("Taller Uno")
                .rut("11111111-1")
                .activo(true)
                .createdAt(now)
                .updatedAt(now)
                .build());
        Taller t2 = em.persist(Taller.builder()
                .planId(plan.getId())
                .nombre("Taller Dos")
                .rut("22222222-2")
                .activo(true)
                .createdAt(now)
                .updatedAt(now)
                .build());

        // Sucursales: S1 and S2 under T1, S3 under T2
        Sucursal s1 = em.persist(Sucursal.builder()
                .tallerId(t1.getId())
                .nombre("Sucursal 1")
                .activo(true)
                .createdAt(now)
                .updatedAt(now)
                .build());
        Sucursal s2 = em.persist(Sucursal.builder()
                .tallerId(t1.getId())
                .nombre("Sucursal 2")
                .activo(true)
                .createdAt(now)
                .updatedAt(now)
                .build());
        Sucursal s3 = em.persist(Sucursal.builder()
                .tallerId(t2.getId())
                .nombre("Sucursal 3")
                .activo(true)
                .createdAt(now)
                .updatedAt(now)
                .build());

        EstadoOrden estado = em.persist(EstadoOrden.builder()
                .codigo("recibida")
                .nombre("Recibida")
                .orden(1)
                .esFinal(false)
                .build());
        TipoOrden tipo = em.persist(TipoOrden.builder()
                .codigo("reparacion")
                .nombre("Reparacion")
                .activo(true)
                .build());
        Cliente cliente1 = em.persist(Cliente.builder()
                .tallerId(t1.getId())
                .nombre("Cliente")
                .apellido("Uno")
                .createdAt(now)
                .updatedAt(now)
                .build());
        Cliente cliente2 = em.persist(Cliente.builder()
                .tallerId(t1.getId())
                .nombre("Cliente")
                .apellido("Dos")
                .createdAt(now)
                .updatedAt(now)
                .build());
        Cliente cliente3 = em.persist(Cliente.builder()
                .tallerId(t2.getId())
                .nombre("Cliente")
                .apellido("Tres")
                .createdAt(now)
                .updatedAt(now)
                .build());
        Bicicleta b1 = em.persist(Bicicleta.builder()
                .clienteId(cliente1.getId())
                .createdAt(now)
                .updatedAt(now)
                .build());
        Bicicleta b2 = em.persist(Bicicleta.builder()
                .clienteId(cliente2.getId())
                .createdAt(now)
                .updatedAt(now)
                .build());
        Bicicleta b3 = em.persist(Bicicleta.builder()
                .clienteId(cliente3.getId())
                .createdAt(now)
                .updatedAt(now)
                .build());

        // Ordenes
        Orden o1 = em.persist(Orden.builder()
                .tallerId(t1.getId())
                .sucursalId(s1.getId())
                .bicicletaId(b1.getId())
                .estadoId(estado.getId())
                .tipoId(tipo.getId())
                .numeroOrden("ORD-001")
                .diagnosticoInicial("Trabajo 1")
                .descuentoManual(BigDecimal.ZERO)
                .porcentajeDescuentoMembresia(BigDecimal.ZERO)
                .fechaIngreso(now.minusHours(2))
                .createdAt(now.minusHours(2))
                .updatedAt(now.minusHours(2))
                .build());
        Orden o2 = em.persist(Orden.builder()
                .tallerId(t1.getId())
                .sucursalId(s2.getId())
                .bicicletaId(b2.getId())
                .estadoId(estado.getId())
                .tipoId(tipo.getId())
                .numeroOrden("ORD-002")
                .diagnosticoInicial("Trabajo 2")
                .descuentoManual(BigDecimal.ZERO)
                .porcentajeDescuentoMembresia(BigDecimal.ZERO)
                .fechaIngreso(now.minusHours(1))
                .createdAt(now.minusHours(1))
                .updatedAt(now.minusHours(1))
                .build());
        Orden o3 = em.persist(Orden.builder()
                .tallerId(t2.getId())
                .sucursalId(s3.getId())
                .bicicletaId(b3.getId())
                .estadoId(estado.getId())
                .tipoId(tipo.getId())
                .numeroOrden("ORD-003")
                .diagnosticoInicial("Trabajo 3")
                .descuentoManual(BigDecimal.ZERO)
                .porcentajeDescuentoMembresia(BigDecimal.ZERO)
                .fechaIngreso(now)
                .createdAt(now)
                .updatedAt(now)
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

    @Test
    void countByClienteIdAndTallerId_returnsCorrectCountAndRespectsTallerId() {
        OffsetDateTime now = OffsetDateTime.now();
        PlanSaas plan = em.persist(PlanSaas.builder()
                .codigo("test")
                .nombre("Test")
                .orden(0)
                .activo(true)
                .build());
        Taller t1 = em.persist(Taller.builder()
                .planId(plan.getId())
                .nombre("Taller Uno")
                .rut("11111111-1")
                .activo(true)
                .createdAt(now)
                .updatedAt(now)
                .build());
        Taller t2 = em.persist(Taller.builder()
                .planId(plan.getId())
                .nombre("Taller Dos")
                .rut("22222222-2")
                .activo(true)
                .createdAt(now)
                .updatedAt(now)
                .build());

        Sucursal s1 = em.persist(Sucursal.builder()
                .tallerId(t1.getId())
                .nombre("Sucursal 1")
                .activo(true)
                .createdAt(now)
                .updatedAt(now)
                .build());
        Sucursal s3 = em.persist(Sucursal.builder()
                .tallerId(t2.getId())
                .nombre("Sucursal 3")
                .activo(true)
                .createdAt(now)
                .updatedAt(now)
                .build());

        EstadoOrden estado = em.persist(EstadoOrden.builder()
                .codigo("recibida")
                .nombre("Recibida")
                .orden(1)
                .esFinal(false)
                .build());
        TipoOrden tipo = em.persist(TipoOrden.builder()
                .codigo("reparacion")
                .nombre("Reparacion")
                .activo(true)
                .build());

        Cliente cliente1 = em.persist(Cliente.builder()
                .tallerId(t1.getId())
                .nombre("Cliente")
                .apellido("Uno")
                .createdAt(now)
                .updatedAt(now)
                .build());
        Cliente cliente2 = em.persist(Cliente.builder()
                .tallerId(t2.getId())
                .nombre("Cliente")
                .apellido("Dos")
                .createdAt(now)
                .updatedAt(now)
                .build());

        Bicicleta b1 = em.persist(Bicicleta.builder()
                .clienteId(cliente1.getId())
                .createdAt(now)
                .updatedAt(now)
                .build());
        Bicicleta b2 = em.persist(Bicicleta.builder()
                .clienteId(cliente2.getId())
                .createdAt(now)
                .updatedAt(now)
                .build());

        em.persist(Orden.builder()
                .tallerId(t1.getId())
                .sucursalId(s1.getId())
                .bicicletaId(b1.getId())
                .estadoId(estado.getId())
                .tipoId(tipo.getId())
                .numeroOrden("ORD-001")
                .descuentoManual(BigDecimal.ZERO)
                .porcentajeDescuentoMembresia(BigDecimal.ZERO)
                .fechaIngreso(now.minusHours(2))
                .createdAt(now.minusHours(2))
                .updatedAt(now.minusHours(2))
                .build());
        em.persist(Orden.builder()
                .tallerId(t1.getId())
                .sucursalId(s1.getId())
                .bicicletaId(b1.getId())
                .estadoId(estado.getId())
                .tipoId(tipo.getId())
                .numeroOrden("ORD-002")
                .descuentoManual(BigDecimal.ZERO)
                .porcentajeDescuentoMembresia(BigDecimal.ZERO)
                .fechaIngreso(now.minusHours(1))
                .createdAt(now.minusHours(1))
                .updatedAt(now.minusHours(1))
                .build());
        em.persist(Orden.builder()
                .tallerId(t2.getId())
                .sucursalId(s3.getId())
                .bicicletaId(b2.getId())
                .estadoId(estado.getId())
                .tipoId(tipo.getId())
                .numeroOrden("ORD-003")
                .descuentoManual(BigDecimal.ZERO)
                .porcentajeDescuentoMembresia(BigDecimal.ZERO)
                .fechaIngreso(now)
                .createdAt(now)
                .updatedAt(now)
                .build());

        em.flush();
        em.clear();

        assertThat(ordenRepository.countByClienteIdAndTallerId(cliente1.getId(), t1.getId())).isEqualTo(2L);
        assertThat(ordenRepository.countByClienteIdAndTallerId(cliente1.getId(), t2.getId())).isEqualTo(0L);
        assertThat(ordenRepository.countByClienteIdAndTallerId(cliente2.getId(), t2.getId())).isEqualTo(1L);
    }

    @Test
    void findResumenByClienteIdAndTallerId_returnsCorrectRowsOrderedDescAndLimitedToFive() {
        OffsetDateTime now = OffsetDateTime.now();
        PlanSaas plan = em.persist(PlanSaas.builder()
                .codigo("test")
                .nombre("Test")
                .orden(0)
                .activo(true)
                .build());
        Taller t1 = em.persist(Taller.builder()
                .planId(plan.getId())
                .nombre("Taller Uno")
                .rut("11111111-1")
                .activo(true)
                .createdAt(now)
                .updatedAt(now)
                .build());
        Taller t2 = em.persist(Taller.builder()
                .planId(plan.getId())
                .nombre("Taller Dos")
                .rut("22222222-2")
                .activo(true)
                .createdAt(now)
                .updatedAt(now)
                .build());

        Sucursal s1 = em.persist(Sucursal.builder()
                .tallerId(t1.getId())
                .nombre("Sucursal 1")
                .activo(true)
                .createdAt(now)
                .updatedAt(now)
                .build());
        Sucursal s3 = em.persist(Sucursal.builder()
                .tallerId(t2.getId())
                .nombre("Sucursal 3")
                .activo(true)
                .createdAt(now)
                .updatedAt(now)
                .build());

        EstadoOrden estado = em.persist(EstadoOrden.builder()
                .codigo("recibida")
                .nombre("Recibida")
                .orden(1)
                .esFinal(false)
                .build());
        TipoOrden tipo = em.persist(TipoOrden.builder()
                .codigo("reparacion")
                .nombre("Reparacion")
                .activo(true)
                .build());

        Cliente cliente1 = em.persist(Cliente.builder()
                .tallerId(t1.getId())
                .nombre("Cliente")
                .apellido("Uno")
                .createdAt(now)
                .updatedAt(now)
                .build());
        Cliente cliente2 = em.persist(Cliente.builder()
                .tallerId(t2.getId())
                .nombre("Cliente")
                .apellido("Dos")
                .createdAt(now)
                .updatedAt(now)
                .build());

        Bicicleta b1 = em.persist(Bicicleta.builder()
                .clienteId(cliente1.getId())
                .createdAt(now)
                .updatedAt(now)
                .build());
        Bicicleta b2 = em.persist(Bicicleta.builder()
                .clienteId(cliente2.getId())
                .createdAt(now)
                .updatedAt(now)
                .build());

        for (int i = 0; i < 6; i++) {
            em.persist(Orden.builder()
                    .tallerId(t1.getId())
                    .sucursalId(s1.getId())
                    .bicicletaId(b1.getId())
                    .estadoId(estado.getId())
                    .tipoId(tipo.getId())
                    .numeroOrden("ORD-" + i)
                    .descuentoManual(BigDecimal.ZERO)
                    .porcentajeDescuentoMembresia(BigDecimal.ZERO)
                    .fechaIngreso(now.minusHours(6 - i))
                    .createdAt(now.minusHours(6 - i))
                    .updatedAt(now.minusHours(6 - i))
                    .build());
        }
        em.persist(Orden.builder()
                .tallerId(t2.getId())
                .sucursalId(s3.getId())
                .bicicletaId(b2.getId())
                .estadoId(estado.getId())
                .tipoId(tipo.getId())
                .numeroOrden("ORD-T2")
                .descuentoManual(BigDecimal.ZERO)
                .porcentajeDescuentoMembresia(BigDecimal.ZERO)
                .fechaIngreso(now)
                .createdAt(now)
                .updatedAt(now)
                .build());

        em.flush();
        em.clear();

        List<OrdenResumenClienteResult> result = ordenRepository.findResumenByClienteIdAndTallerId(
                cliente1.getId(), t1.getId(), Pageable.ofSize(5));

        assertThat(result).hasSize(5);
        assertThat(result).extracting(OrdenResumenClienteResult::numeroOrden)
                .containsExactly("ORD-5", "ORD-4", "ORD-3", "ORD-2", "ORD-1");
        assertThat(result).extracting(OrdenResumenClienteResult::tipoOrden)
                .containsOnly("Reparacion");
        assertThat(result).extracting(OrdenResumenClienteResult::estadoOrden)
                .containsOnly("Recibida");

        // Tenant isolation
        List<OrdenResumenClienteResult> resultT2 = ordenRepository.findResumenByClienteIdAndTallerId(
                cliente1.getId(), t2.getId(), Pageable.ofSize(5));
        assertThat(resultT2).isEmpty();
    }
}
