package com.ecomaxtienda.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecomaxtienda.entity.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    
    // Buscar productos activos
    List<Producto> findByEstadoTrue();
    
    // Buscar por categoría
    List<Producto> findByCategoria(String categoria);
    
    // Buscar productos activos por categoría
    List<Producto> findByCategoriaAndEstadoTrue(String categoria);
    
    // Buscar por marca
    List<Producto> findByMarca(String marca);
    
    // Buscar por rango de precios
    List<Producto> findByPrecioBetween(BigDecimal precioMin, BigDecimal precioMax);
    
    // Buscar productos eco-amigables
    @Query("SELECT p FROM Producto p WHERE p.puntuacionEco >= :puntuacionMinima AND p.estado = true")
    List<Producto> findProductosEcoAmigables(@Param("puntuacionMinima") BigDecimal puntuacionMinima);
    
    // Búsqueda general de productos
    @Query("SELECT p FROM Producto p WHERE " +
           "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(p.categoria) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(p.marca) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    List<Producto> buscarProductos(@Param("busqueda") String busqueda);
    
    // Productos con stock disponible
    @Query("SELECT p FROM Producto p JOIN p.inventario i WHERE i.stock > 0 AND p.estado = true")
    List<Producto> findProductosConStock();
    
    // Productos con stock bajo
    @Query("SELECT p FROM Producto p JOIN p.inventario i WHERE i.stock <= i.stockMinimo AND p.estado = true")
    List<Producto> findProductosConStockBajo();
    
    // Productos agotados
    @Query("SELECT p FROM Producto p JOIN p.inventario i WHERE i.stock = 0 AND p.estado = true")
    List<Producto> findProductosAgotados();
    
    // Productos más vendidos
    @Query("SELECT p FROM Producto p JOIN p.pedidoDetalles pd " +
           "GROUP BY p ORDER BY SUM(pd.cantidad) DESC")
    List<Producto> findProductosMasVendidos(Pageable pageable);
    
    // Productos por categoría con paginación
    Page<Producto> findByCategoriaAndEstadoTrue(String categoria, Pageable pageable);
    
    // Productos por rango de precios con paginación
    Page<Producto> findByPrecioBetweenAndEstadoTrue(BigDecimal precioMin, BigDecimal precioMax, Pageable pageable);
    
    // Contar productos por categoría
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.categoria = :categoria AND p.estado = true")
    Long contarPorCategoria(@Param("categoria") String categoria);
    
    // Obtener categorías únicas
    @Query("SELECT DISTINCT p.categoria FROM Producto p WHERE p.estado = true ORDER BY p.categoria")
    List<String> findCategorias();
    
    // Obtener marcas únicas
    @Query("SELECT DISTINCT p.marca FROM Producto p WHERE p.estado = true ORDER BY p.marca")
    List<String> findMarcas();
    
    // Estadísticas de productos
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.estado = true")
    Long contarProductosActivos();
    
    @Query("SELECT AVG(p.precio) FROM Producto p WHERE p.estado = true")
    BigDecimal obtenerPrecioPromedio();
    
    @Query("SELECT AVG(p.puntuacionEco) FROM Producto p WHERE p.puntuacionEco IS NOT NULL AND p.estado = true")
    BigDecimal obtenerPuntuacionEcoPromedio();
    
    // Productos recomendados (alta puntuación eco y precio razonable)
    @Query("SELECT p FROM Producto p WHERE p.puntuacionEco >= 7.0 AND p.precio <= :precioMaximo AND p.estado = true")
    List<Producto> findProductosRecomendados(@Param("precioMaximo") BigDecimal precioMaximo);
}
