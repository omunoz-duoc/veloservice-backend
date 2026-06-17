package com.veloservice.inventario.application.usecase;

import com.veloservice.administracion.domain.model.Sucursal;
import com.veloservice.administracion.infraestructure.persistence.repository.SucursalRepository;
import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.TallerContext;
import com.veloservice.inventario.domain.model.CategoriaProducto;
import com.veloservice.inventario.domain.model.Producto;
import com.veloservice.inventario.infraestructure.persistence.repository.CategoriaProductoRepository;
import com.veloservice.inventario.application.usecase.StockMovimientoService;
import com.veloservice.inventario.infraestructure.persistence.repository.ProductoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock private ProductoRepository productoRepository;
    @Mock private StockMovimientoService stockMovimientoService;
    @Mock private CategoriaProductoRepository categoriaProductoRepository;
    @Mock private SucursalRepository sucursalRepository;
    @Mock private EntityManager entityManager;

    private ProductoService productoService;

    @BeforeEach
    void setUp() {
        productoService = new ProductoService(productoRepository, stockMovimientoService, categoriaProductoRepository, sucursalRepository, entityManager);
    }

    @AfterEach
    void tearDown() {
        SucursalContext.clear();
        TallerContext.clear();
    }

    @Test
    void listarReturnsActiveProductsFromCurrentSucursal() {
        UUID sucursalId = UUID.fromString("11000000-0000-4000-8000-000000000001");
        UUID categoriaId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        given(productoRepository.findBySucursalIdAndActivoTrueOrderByNombreAsc(sucursalId))
                .willReturn(List.of(producto(sucursalId, categoriaId, "Cadena Shimano HG601 11v", "CAD-HG601", "Shimano")));
        given(categoriaProductoRepository.findById(categoriaId))
                .willReturn(Optional.of(CategoriaProducto.builder().id(categoriaId).nombre("Transmision").build()));

        var productos = productoService.listar();

        assertThat(productos).hasSize(1);
        assertThat(productos.getFirst().getNombre()).isEqualTo("Cadena Shimano HG601 11v");
        assertThat(productos.getFirst().getMarca()).isEqualTo("Shimano");
        assertThat(productos.getFirst().getCategoriaNombre()).isEqualTo("Transmision");
        verify(productoRepository).findBySucursalIdAndActivoTrueOrderByNombreAsc(sucursalId);
    }

    @Test
    void buscarUsesCurrentSucursalAndSearchesCatalogFields() {
        UUID sucursalId = UUID.fromString("11000000-0000-4000-8000-000000000001");
        SucursalContext.setCurrentSucursal(sucursalId);
        given(productoRepository.searchBySucursalId(sucursalId, "shimano"))
                .willReturn(List.of(
                        producto(sucursalId, null, "Cadena Shimano HG601 11v", "CAD-HG601", "Shimano"),
                        producto(sucursalId, null, "Pastillas Shimano B05S resina", "BRK-B05S", "Shimano")
                ));

        var productos = productoService.buscar(" shimano ");

        assertThat(productos)
                .extracting("nombre")
                .containsExactly("Cadena Shimano HG601 11v", "Pastillas Shimano B05S resina");
        verify(productoRepository).searchBySucursalId(sucursalId, "shimano");
    }

    @Test
    void buscarWithoutSucursalReturnsEmptyList() {
        assertThat(productoService.buscar("cadena")).isEmpty();
    }

    @Test
    void actualizarPersistsProductFromCurrentSucursal() {
        UUID sucursalId = UUID.fromString("11000000-0000-4000-8000-000000000001");
        UUID productoId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        Producto existente = producto(sucursalId, null, "Cadena Shimano HG601 11v", "CAD-HG601", "Shimano");
        existente.setId(productoId);
        given(productoRepository.findByIdAndSucursalId(productoId, sucursalId)).willReturn(Optional.of(existente));
        given(productoRepository.save(existente)).willReturn(existente);

        var actualizado = productoService.actualizar(productoId, new com.veloservice.inventario.application.dto.ProductoCreateCommand(
                "Cadena Shimano HG701",
                "SH-HG701",
                "Shimano",
                "unidad",
                new BigDecimal("18000"),
                new BigDecimal("27900"),
                6,
                2,
                null
        ));

        assertThat(actualizado.getNombre()).isEqualTo("Cadena Shimano HG701");
        assertThat(actualizado.getSku()).isEqualTo("SH-HG701");
        assertThat(actualizado.getPrecioCosto()).isEqualByComparingTo("18000");
        assertThat(actualizado.getPrecioVenta()).isEqualByComparingTo("27900");
        assertThat(actualizado.getStock()).isEqualTo(6);
        verify(productoRepository).save(existente);
    }

        @Test
        void listarUsesExplicitSucursalWhenProvided() {
                UUID tallerId = UUID.fromString("10000000-0000-4000-8000-000000000001");
                UUID sucursalId = UUID.fromString("11000000-0000-4000-8000-000000000001");
                TallerContext.setCurrentTaller(tallerId);
                given(sucursalRepository.existsByIdAndTallerId(sucursalId, tallerId)).willReturn(true);
                given(productoRepository.findBySucursalIdAndActivoTrueOrderByNombreAsc(sucursalId))
                                .willReturn(List.of(producto(sucursalId, null, "Cadena Shimano HG601 11v", "CAD-HG601", "Shimano")));

                var productos = productoService.listar(sucursalId);

                assertThat(productos)
                                .extracting("nombre")
                                .containsExactly("Cadena Shimano HG601 11v");
                verify(productoRepository).findBySucursalIdAndActivoTrueOrderByNombreAsc(sucursalId);
        }

        @Test
        void buscarWithExplicitSucursalAndSearchTextUsesProvidedSucursal() {
                UUID tallerId = UUID.fromString("10000000-0000-4000-8000-000000000001");
                UUID sucursalId = UUID.fromString("11000000-0000-4000-8000-000000000001");
                TallerContext.setCurrentTaller(tallerId);
                given(sucursalRepository.existsByIdAndTallerId(sucursalId, tallerId)).willReturn(true);
                given(productoRepository.searchBySucursalId(sucursalId, "cadena"))
                                .willReturn(List.of(producto(sucursalId, null, "Cadena Shimano HG601 11v", "CAD-HG601", "Shimano")));

                var productos = productoService.buscar("cadena", sucursalId);

                assertThat(productos)
                                .extracting("nombre")
                                .containsExactly("Cadena Shimano HG601 11v");
                verify(productoRepository).searchBySucursalId(sucursalId, "cadena");
        }

        @Test
        void explicitSucursalOutsideTallerIsRejected() {
                UUID tallerId = UUID.fromString("10000000-0000-4000-8000-000000000001");
                UUID sucursalId = UUID.fromString("11000000-0000-4000-8000-000000000001");
                TallerContext.setCurrentTaller(tallerId);
                given(sucursalRepository.existsByIdAndTallerId(sucursalId, tallerId)).willReturn(false);

                assertThrows(IllegalArgumentException.class, () -> productoService.listar(sucursalId));
        }

    @Test
    void buscarFallsBackToActiveSucursalWhenOnlyTallerContextExists() {
        UUID tallerId = UUID.fromString("10000000-0000-4000-8000-000000000001");
        UUID sucursalId = UUID.fromString("11000000-0000-4000-8000-000000000001");
        TallerContext.setCurrentTaller(tallerId);
        Query query = mock(Query.class);
        given(sucursalRepository.findFirstByTallerIdAndActivoTrueOrderByCreatedAtAsc(tallerId))
                .willReturn(Optional.of(Sucursal.builder().id(sucursalId).tallerId(tallerId).activo(true).build()));
        given(entityManager.createNativeQuery(contains("current_sucursal_id"))).willReturn(query);
        given(query.setParameter(eq(1), anyString())).willReturn(query);
        given(query.getSingleResult()).willReturn(null);
        given(productoRepository.searchBySucursalId(sucursalId, "cadena"))
                .willReturn(List.of(producto(sucursalId, null, "Cadena Shimano HG601 11v", "CAD-HG601", "Shimano")));

        var productos = productoService.buscar("cadena");

        assertThat(productos)
                .extracting("nombre")
                .containsExactly("Cadena Shimano HG601 11v");
        verify(query).setParameter(1, sucursalId.toString());
        verify(productoRepository).searchBySucursalId(sucursalId, "cadena");
    }

    private Producto producto(UUID sucursalId, UUID categoriaId, String nombre, String sku, String marca) {
        return Producto.builder()
                .id(UUID.randomUUID())
                .sucursalId(sucursalId)
                .categoriaId(categoriaId)
                .nombre(nombre)
                .sku(sku)
                .marca(marca)
                .unidadMedida("unidad")
                .precioCosto(new BigDecimal("15000.00"))
                .precioVenta(new BigDecimal("18900.00"))
                .stock(10)
                .stockMinimo(2)
                .activo(true)
                .build();
    }
}
