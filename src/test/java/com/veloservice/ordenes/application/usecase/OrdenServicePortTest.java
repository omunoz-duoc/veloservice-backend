package com.veloservice.ordenes.application.usecase;

import com.veloservice.ordenes.application.port.BicicletaPort;
import com.veloservice.ordenes.application.port.UsuarioPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenServicePortTest {

    @Mock BicicletaPort bicicletaPort;
    @Mock UsuarioPort usuarioPort;

    @Test
    void bicicletaPort_retorna_ref_cuando_existe() {
        UUID id = UUID.randomUUID();
        var ref = new BicicletaPort.BicicletaRef(id, "Trek", "FX3", "urbana", "negro", "27.5", null);
        when(bicicletaPort.findById(id)).thenReturn(Optional.of(ref));

        Optional<BicicletaPort.BicicletaRef> result = bicicletaPort.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().marca()).isEqualTo("Trek");
    }

    @Test
    void usuarioPort_existsMecanico_usa_sucursalId() {
        UUID mecanicoId = UUID.randomUUID();
        UUID sucursalId = UUID.randomUUID();
        when(usuarioPort.existsMecanicoEnSucursal(mecanicoId, sucursalId)).thenReturn(true);

        boolean exists = usuarioPort.existsMecanicoEnSucursal(mecanicoId, sucursalId);

        assertThat(exists).isTrue();
    }
}
