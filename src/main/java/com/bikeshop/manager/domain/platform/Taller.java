package com.bikeshop.manager.domain.platform;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

/**
 * Represents a bike workshop.
 */
@Entity
@Table(name = "talleres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Taller {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 150)
    private String nombre;
    @Column(nullable = false, length = 20, unique = true)
    private String rut;
    @Column(length = 20)
    private String telefono;
    @Column(length = 150)
    private String email;
    @Column(name = "plan_saas", length = 100)
    private String planSaas;
    @Column(name = "logo_url", length = 255)
    private String logoUrl;
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
