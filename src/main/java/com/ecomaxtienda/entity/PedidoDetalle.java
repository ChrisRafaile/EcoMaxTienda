package com.ecomaxtienda.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_pedido_detalle")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "descuento_unitario", precision = 10, scale = 2)
    private BigDecimal descuentoUnitario = BigDecimal.ZERO;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "notas", length = 500)
    private String notas;

    @PrePersist
    public void prePersist() {
        if (this.descuentoUnitario == null) {
            this.descuentoUnitario = BigDecimal.ZERO;
        }
        this.calcularSubtotal();
    }

    @PreUpdate
    public void preUpdate() {
        this.calcularSubtotal();
    }

    // Métodos de utilidad
    public void calcularSubtotal() {
        if (this.cantidad != null && this.precioUnitario != null) {
            BigDecimal precioConDescuento = this.precioUnitario.subtract(this.descuentoUnitario);
            this.subtotal = precioConDescuento.multiply(BigDecimal.valueOf(this.cantidad));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    public BigDecimal getTotalDescuento() {
        return this.descuentoUnitario.multiply(BigDecimal.valueOf(this.cantidad));
    }

    public BigDecimal getPrecioFinalUnitario() {
        return this.precioUnitario.subtract(this.descuentoUnitario);
    }

    public BigDecimal getAhorro() {
        return this.getTotalDescuento();
    }

    public boolean tieneDescuento() {
        return this.descuentoUnitario != null && this.descuentoUnitario.compareTo(BigDecimal.ZERO) > 0;
    }    public BigDecimal getPorcentajeDescuento() {
        if (this.precioUnitario == null || this.precioUnitario.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return this.descuentoUnitario.divide(this.precioUnitario, 4, java.math.RoundingMode.HALF_UP)
               .multiply(BigDecimal.valueOf(100));
    }
}
