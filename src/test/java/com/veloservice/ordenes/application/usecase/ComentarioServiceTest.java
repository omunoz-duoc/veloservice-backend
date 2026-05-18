package com.veloservice.ordenes.application.usecase;

import com.veloservice.administracion.domain.model.Usuario;
import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.security.UsuarioContext;
import com.veloservice.ordenes.application.dto.ComentarioResult;
import com.veloservice.ordenes.domain.model.Comentario;
import com.veloservice.ordenes.infraestructure.persistence.repository.ComentarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComentarioServiceTest {

    @Mock private ComentarioRepository comentarioRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private ComentarioService comentarioService;

    @Test
    void listarPorOrdenReturnsAuthorName() {
        UUID ordenId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        Comentario comentario = Comentario.builder()
                .id(UUID.randomUUID())
                .ordenId(ordenId)
                .usuarioId(usuarioId)
                .texto("Repuesto solicitado")
                .createdAt(OffsetDateTime.now())
                .build();

        Usuario usuario = new Usuario();
        usuario.setNombre("Juan");
        usuario.setApellido("Pérez");

        when(comentarioRepository.findByOrdenIdOrderByCreatedAtAsc(ordenId))
                .thenReturn(List.of(comentario));
        when(usuarioRepository.findById(usuarioId))
                .thenReturn(Optional.of(usuario));

        List<ComentarioResult> results = comentarioService.listarPorOrden(ordenId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAutor()).isEqualTo("Juan Pérez");
        assertThat(results.get(0).getTexto()).isEqualTo("Repuesto solicitado");
    }

    @Test
    void agregarSavesComentarioWithCurrentUser() {
        UUID ordenId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        Usuario usuario = new Usuario();
        usuario.setNombre("Ana");
        usuario.setApellido("Gómez");

        Comentario saved = Comentario.builder()
                .id(UUID.randomUUID())
                .ordenId(ordenId)
                .usuarioId(usuarioId)
                .texto("Texto de prueba")
                .createdAt(OffsetDateTime.now())
                .build();

        when(comentarioRepository.save(any(Comentario.class))).thenReturn(saved);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        try (MockedStatic<UsuarioContext> ctx = mockStatic(UsuarioContext.class)) {
            ctx.when(UsuarioContext::getCurrentUser).thenReturn(usuarioId);

            ComentarioResult result = comentarioService.agregar(ordenId, "Texto de prueba");

            assertThat(result.getAutor()).isEqualTo("Ana Gómez");
            verify(comentarioRepository).save(any(Comentario.class));
        }
    }
}
