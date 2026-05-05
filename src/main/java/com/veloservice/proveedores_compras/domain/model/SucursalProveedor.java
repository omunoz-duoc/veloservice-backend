package com.veloservice.proveedores_compras.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents the link between a sucursal (branch) and a supplier/provider.
 * Maps to sucursal_proveedores table.
 */
@Entity
@Table(name = "sucursal_proveedores", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sucursal_id", "proveedor_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SucursalProveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sucursal_id", nullable = false)
    private UUID sucursalId;

    @Column(name = "proveedor_id", nullable = false)
    private UUID proveedorId;

    @Column(name = "codigo_cliente")
    private String codigoCliente;

    @Column(name = "condicion_pago")
    private String condicionPago;

    @Column(name = "contacto_asignado")
    private String contactoAsignado;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}