package com.veloservice.ordenes.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents a comment on a work order.
 */
@Entity
@Table(name = "comentarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "orden_id", nullable = false)
    private UUID ordenId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
