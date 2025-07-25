package com.ecomaxtienda.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inventario")
    private Integer idInventario;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false, unique = true)
    private Producto producto;

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 5;

    @Column(name = "stock_maximo")
    private Integer stockMaximo;

    @Column(name = "ubicacion", length = 100)
    private String ubicacion;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @Column(name = "usuario_actualizacion", length = 100)
    private String usuarioActualizacion;

    @PrePersist
    public void prePersist() {
        if (this.fechaActualizacion == null) {
            this.fechaActualizacion = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = true;
        }
        if (this.stock == null) {
            this.stock = 0;
        }
        if (this.stockMinimo == null) {
            this.stockMinimo = 5;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    // Métodos de utilidad
    public boolean necesitaReposicion() {
        return this.stock <= this.stockMinimo;
    }

    public boolean agotado() {
        return this.stock <= 0;
    }

    public boolean stockCritico() {
        return this.stock > 0 && this.stock <= (this.stockMinimo / 2);
    }

    public void reducirStock(Integer cantidad) {
        if (cantidad > 0 && this.stock >= cantidad) {
            this.stock -= cantidad;
        }
    }

    public void aumentarStock(Integer cantidad) {
        if (cantidad > 0) {
            this.stock += cantidad;
        }
    }

    public String getEstadoStock() {
        if (this.agotado()) {
            return "AGOTADO";
        } else if (this.stockCritico()) {
            return "CRITICO";
        } else if (this.necesitaReposicion()) {
            return "BAJO";
        } else {
            return "NORMAL";
        }
    }    // Constructor personalizado para casos de uso específicos
    public Inventario(Producto producto, Integer stock, Integer stockMinimo, Integer stockMaximo) {
        this.producto = producto;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.stockMaximo = stockMaximo;
        this.estado = true;
        this.fechaActualizacion = LocalDateTime.now();    }
}
