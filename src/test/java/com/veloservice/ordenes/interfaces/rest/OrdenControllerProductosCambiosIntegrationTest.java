package com.veloservice.ordenes.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veloservice.BikeshopManagerApplication;
import com.veloservice.clientes.domain.model.Bicicleta;
import com.veloservice.clientes.domain.model.Cliente;
import com.veloservice.clientes.infraestructure.persistence.repository.BicicletaRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.ClienteRepository;
import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.TallerContext;
import com.veloservice.config.tenant.TenantOperationAspect;
import com.veloservice.config.tenant.UsuarioContext;
import com.veloservice.inventario.domain.model.Producto;
import com.veloservice.inventario.infraestructure.persistence.repository.ProductoRepository;
import com.veloservice.ordenes.domain.model.EstadoOrden;
import com.veloservice.ordenes.domain.model.Orden;
import com.veloservice.ordenes.domain.model.OrdenProducto;
import com.veloservice.ordenes.domain.model.TipoOrden;
import com.veloservice.ordenes.infraestructure.persistence.repository.EstadoOrdenCatalogRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenProductoRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.TipoOrdenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BikeshopManagerApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class OrdenControllerProductosCambiosIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EstadoOrdenCatalogRepository estadoOrdenRepository;
    @Autowired private TipoOrdenRepository tipoOrdenRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private BicicletaRepository bicicletaRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private OrdenRepository ordenRepository;
    @Autowired private OrdenProductoRepository ordenProductoRepository;

    @MockBean private TenantOperationAspect tenantOperationAspect;

    @BeforeEach
    void setUp() throws Throwable {
        doAnswer(invocation -> ((ProceedingJoinPoint) invocation.getArgument(0)).proceed())
                .when(tenantOperationAspect).applySucursalContext(any(ProceedingJoinPoint.class));
    }

    @AfterEach
    void tearDown() {
        SucursalContext.clear();
        TallerContext.clear();
        UsuarioContext.clear();
    }

    @Test
    @WithMockUser(roles = "mecanico")
    void patchProductosCambiosEliminarDeletesLineAndRemovesItFromDetail() throws Exception {
        OffsetDateTime now = OffsetDateTime.now();
        UUID tallerId = UUID.randomUUID();
        UUID sucursalId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);

        EstadoOrden estado = estadoOrdenRepository.save(EstadoOrden.builder()
                .codigo("recibida-" + UUID.randomUUID())
                .nombre("Recibida")
                .orden(1)
                .esFinal(false)
                .build());
        TipoOrden tipo = tipoOrdenRepository.save(TipoOrden.builder()
                .codigo("reparacion-" + UUID.randomUUID())
                .nombre("Reparacion")
                .activo(true)
                .build());
        Cliente cliente = clienteRepository.save(Cliente.builder()
                .tallerId(tallerId)
                .codigoCliente("CL-0001")
                .nombre("Cliente")
                .apellido("Demo")
                .createdAt(now)
                .updatedAt(now)
                .build());
        Bicicleta bicicleta = bicicletaRepository.save(Bicicleta.builder()
                .clienteId(cliente.getId())
                .marca("Trek")
                .modelo("Domane")
                .tipo("Ruta")
                .createdAt(now)
                .updatedAt(now)
                .build());
        Producto producto = productoRepository.save(Producto.builder()
                .sucursalId(sucursalId)
                .nombre("Cadena")
                .sku("CAD-" + UUID.randomUUID())
                .unidadMedida("unidad")
                .precioCosto(new BigDecimal("7000.00"))
                .precioVenta(new BigDecimal("12500.00"))
                .stock(5)
                .stockMinimo(1)
                .activo(true)
                .createdAt(now)
                .updatedAt(now)
                .build());
        Orden orden = ordenRepository.save(Orden.builder()
                .tallerId(tallerId)
                .sucursalId(sucursalId)
                .bicicletaId(bicicleta.getId())
                .estadoId(estado.getId())
                .tipoId(tipo.getId())
                .numeroOrden("OT-" + UUID.randomUUID())
                .diagnosticoInicial("Diagnostico")
                .descuentoManual(BigDecimal.ZERO)
                .porcentajeDescuentoMembresia(BigDecimal.ZERO)
                .prioridad("media")
                .fechaIngreso(now)
                .createdAt(now)
                .updatedAt(now)
                .build());
        OrdenProducto lineItem = ordenProductoRepository.save(OrdenProducto.builder()
                .ordenId(orden.getId())
                .productoId(producto.getId())
                .cantidad(1)
                .precioCostoSnapshot(producto.getPrecioCosto())
                .precioVentaSnapshot(producto.getPrecioVenta())
                .precioAplicado(producto.getPrecioVenta())
                .proporcionadoPorCliente(false)
                .createdAt(now)
                .build());

        assertThat(ordenProductoRepository.existsById(lineItem.getId())).isTrue();

        String body = objectMapper.writeValueAsString(Map.of(
                "productosCambios", List.of(Map.of(
                        "accion", "ELIMINAR",
                        "lineaId", lineItem.getId()
                ))
        ));

        mockMvc.perform(patch("/ordenes/{id}", orden.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productos").isEmpty());

        assertThat(ordenProductoRepository.existsById(lineItem.getId())).isFalse();

        mockMvc.perform(get("/ordenes/{id}", orden.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productos").isEmpty());
    }
}
