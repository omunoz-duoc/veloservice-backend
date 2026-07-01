package com.veloservice.proveedores_compras.domain.model;

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

/**
 * Condiciones comerciales de un proveedor para una sucursal concreta.
 */
@Entity
@Table(
    name = "sucursal_proveedores",
    indexes = {
        @Index(name = "idx_sucursal_proveedores_sucursal", columnList = "sucursal_id"),
        @Index(name = "idx_sucursal_proveedores_proveedor", columnList = "proveedor_id")
},
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_sucursal_proveedores_sucursal_proveedor", columnNames = {"sucursal_id", "proveedor_id"})
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SucursalProveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
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

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
