package com.veloservice.inventario.application.usecase;

import com.veloservice.config.tenant.UsuarioContext;
import com.veloservice.inventario.domain.TipoMovimientoEnum;
import com.veloservice.inventario.domain.model.MovimientoStock;
import com.veloservice.inventario.infraestructure.persistence.repository.MovimientoStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockMovimientoService {

    private final MovimientoStockRepository movimientoRepository;

    public void registrar(
            UUID productoId,
            UUID ordenId,
            UUID compraId,
            UUID trasladoId,
            TipoMovimientoEnum tipo,
            int cantidad,
            int stockAnterior,
            int stockPosterior,
            String motivo
    ) {
        movimientoRepository.save(MovimientoStock.builder()
                .productoId(productoId)
                .ordenId(ordenId)
                .compraId(compraId)
                .trasladoId(trasladoId)
                .usuarioId(UsuarioContext.getCurrentUser())
                .tipo(tipo)
                .cantidad(cantidad)
                .stockAnterior(stockAnterior)
                .stockPosterior(stockPosterior)
                .motivo(motivo)
                .createdAt(OffsetDateTime.now())
                .build());
    }
}
