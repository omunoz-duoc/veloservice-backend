package com.veloservice.inventario.interfaces.rest;

import com.veloservice.auth.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.security.JwtTokenProvider;
import com.veloservice.inventario.application.dto.ProductoResult;
import com.veloservice.inventario.application.usecase.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductoSearchControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private ProductoService productoService;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private UsuarioRepository usuarioRepository;
    @MockBean private com.veloservice.administracion.infraestructure.persistence.repository.SucursalRepository sucursalRepository;

    @Test
    void searchReturnsFilteredProducts() throws Exception {
        UUID sucursalId = UUID.fromString("11000000-0000-4000-8000-000000000001");
        ProductoResult r = ProductoResult.builder()
                .id(UUID.randomUUID())
                .nombre("Cadena Shimano HG601")
                .sku("SHM-HG601-11")
                .marca("Shimano")
                .precioVenta(new BigDecimal("18900"))
                .stock(4)
                .build();

        when(productoService.buscar("shimano", sucursalId)).thenReturn(List.of(r));

        mockMvc.perform(get("/productos").param("search", "shimano").param("sucursalId", sucursalId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productos[0].nombre").value("Cadena Shimano HG601"))
                .andExpect(jsonPath("$.productos[0].sku").value("SHM-HG601-11"))
                .andExpect(jsonPath("$.productos[0].marca").value("Shimano"))
                .andExpect(jsonPath("$.productos[0].precioVenta").value(18900))
                .andExpect(jsonPath("$.productos[0].precio_asignado").value(18900))
                .andExpect(jsonPath("$.productos[0].stock").value(4));
    }

    @Test
    void listarWithoutSearchReturnsAll() throws Exception {
        UUID sucursalId = UUID.fromString("11000000-0000-4000-8000-000000000001");
        ProductoResult r = ProductoResult.builder()
                .id(UUID.randomUUID())
                .nombre("Llanta MTB")
                .sku("LLT-MTB-29")
                .precioVenta(new BigDecimal("45000"))
                .stock(2)
                .build();

        when(productoService.listar(sucursalId)).thenReturn(List.of(r));

        mockMvc.perform(get("/productos").param("sucursalId", sucursalId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productos[0].nombre").value("Llanta MTB"));
    }

    @Test
    void actualizarReturnsUpdatedProduct() throws Exception {
        UUID productoId = UUID.fromString("24000000-0000-4000-8000-000000000001");
        ProductoResult r = ProductoResult.builder()
                .id(productoId)
                .nombre("Cadena Shimano HG701")
                .sku("SH-HG701")
                .marca("Shimano")
                .precioCosto(new BigDecimal("18000"))
                .precioVenta(new BigDecimal("27900"))
                .stock(6)
                .stockMinimo(2)
                .build();

        when(productoService.actualizar(eq(productoId), any())).thenReturn(r);

        mockMvc.perform(put("/productos/{id}", productoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Cadena Shimano HG701",
                                  "sku": "SH-HG701",
                                  "marca": "Shimano",
                                  "unidadMedida": "unidad",
                                  "precioCosto": 18000,
                                  "precioVenta": 27900,
                                  "stock": 6,
                                  "stockMinimo": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productoId.toString()))
                .andExpect(jsonPath("$.nombre").value("Cadena Shimano HG701"))
                .andExpect(jsonPath("$.precioVenta").value(27900))
                .andExpect(jsonPath("$.precio_asignado").value(27900))
                .andExpect(jsonPath("$.stock_minimo").value(2));
    }
}
