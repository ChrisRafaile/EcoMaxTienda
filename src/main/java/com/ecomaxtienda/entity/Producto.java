package com.ecomaxtienda.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "categoria", nullable = false, length = 50)
    private String categoria;

    @Column(name = "marca", length = 50)
    private String marca;

    @Column(name = "modelo", length = 50)
    private String modelo;

    @Column(name = "color", length = 30)
    private String color;

    @Column(name = "peso", precision = 8, scale = 2)
    private BigDecimal peso;

    @Column(name = "dimensiones", length = 100)
    private String dimensiones;

    @Column(name = "material", length = 50)
    private String material;

    @Column(name = "garantia_meses")
    private Integer garantiaMeses;

    @Column(name = "eficiencia_energetica", length = 10)
    private String eficienciaEnergetica;

    @Column(name = "impacto_ambiental", length = 20)
    private String impactoAmbiental;

    @Column(name = "puntuacion_eco", precision = 3, scale = 2)
    private BigDecimal puntuacionEco;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @OneToOne(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Inventario inventario;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PedidoDetalle> pedidoDetalles;

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    // Métodos de utilidad
    public boolean tieneStock() {
        return this.inventario != null && this.inventario.getStock() > 0;
    }

    public Integer getStockDisponible() {
        if (this.inventario != null && this.inventario.getStock() != null) {
            return this.inventario.getStock();
        }
        return 0;
    }    public boolean esEcoAmigable() {
        return this.puntuacionEco != null && this.puntuacionEco.compareTo(BigDecimal.valueOf(7.0)) >= 0;
    }

    // Constructor personalizado para casos de uso específicos
    public Producto(String nombre, String descripcion, BigDecimal precio, String categoria, String marca) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.marca = marca;
        this.estado = true;
        this.fechaCreacion = LocalDateTime.now();
    }
}
