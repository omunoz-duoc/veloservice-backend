package com.veloservice.ordenes.infraestructure.adapter;

import com.veloservice.clientes.infraestructure.persistence.repository.BicicletaRepository;
import com.veloservice.ordenes.application.port.BicicletaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BicicletaAdapter implements BicicletaPort {

    private final BicicletaRepository bicicletaRepository;

    @Override
    public Optional<BicicletaRef> findById(UUID id) {
        return bicicletaRepository.findById(id).map(b -> {
            ClienteRef clienteRef = null;
            if (b.getCliente() != null) {
                clienteRef = new ClienteRef(
                    b.getCliente().getNombre(),
                    b.getCliente().getApellido(),
                    b.getCliente().getTelefono()
                );
            }
            return new BicicletaRef(
                b.getId(),
                b.getMarca(),
                b.getModelo(),
                b.getTipo(),
                b.getColor(),
                b.getAro(),
                clienteRef
            );
        });
    }
}
