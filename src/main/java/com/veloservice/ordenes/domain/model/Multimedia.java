package com.veloservice.ordenes.domain.model;

import com.veloservice.ordenes.domain.EtapaMultimediaEnum;
import com.veloservice.ordenes.domain.TipoArchivoEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents multimedia evidence attached to a work order.
 */
@Entity
@Table(name = "multimedia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Multimedia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "orden_id", nullable = false)
    private UUID ordenId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_archivo", nullable = false)
    private TipoArchivoEnum tipoArchivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EtapaMultimediaEnum etapa;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}