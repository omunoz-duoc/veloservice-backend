package com.veloservice.ordenes.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.veloservice.ordenes.domain.TipoArchivoEnum;
import com.veloservice.ordenes.domain.EtapaMultimediaEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
/**
 * Archivo multimedia adjunto a una orden durante una etapa del servicio.
 */
@Entity
@Table(
    name = "multimedia",
    indexes = {
        @Index(name = "idx_multimedia_orden", columnList = "orden_id")
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Multimedia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "orden_id", nullable = false)
    private UUID ordenId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "url", nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_archivo", nullable = false)
    private TipoArchivoEnum tipoArchivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa", nullable = false)
    private EtapaMultimediaEnum etapa;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
