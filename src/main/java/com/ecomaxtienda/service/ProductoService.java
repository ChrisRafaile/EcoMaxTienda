package com.ecomaxtienda.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecomaxtienda.entity.Inventario;
import com.ecomaxtienda.entity.Producto;
import com.ecomaxtienda.repository.InventarioRepository;
import com.ecomaxtienda.repository.ProductoRepository;

@Service
@Transactional
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;

    // Constructor manual
    public ProductoService(ProductoRepository productoRepository, InventarioRepository inventarioRepository) {
        this.productoRepository = productoRepository;
        this.inventarioRepository = inventarioRepository;
    }

    // Métodos CRUD básicos
    public List<Producto> findAll() {
        return this.productoRepository.findAll();
    }
    
    public Optional<Producto> findById(Integer id) {
        return this.productoRepository.findById(id);
    }
    
    public Producto save(Producto producto) {
        return this.productoRepository.save(producto);
    }
    
    public void deleteById(Integer id) {
        this.productoRepository.deleteById(id);
    }
    
    // Métodos de negocio
    public List<Producto> obtenerProductosActivos() {
        return this.productoRepository.findByEstadoTrue();
    }
    
    public List<Producto> obtenerPorCategoria(String categoria) {
        return this.productoRepository.findByCategoriaAndEstadoTrue(categoria);
    }
    
    public Page<Producto> obtenerPorCategoriaPaginado(String categoria, Pageable pageable) {
        return this.productoRepository.findByCategoriaAndEstadoTrue(categoria, pageable);
    }
    
    public List<Producto> obtenerPorMarca(String marca) {
        return this.productoRepository.findByMarca(marca);
    }
    
    public List<Producto> obtenerPorRangoPrecios(BigDecimal precioMin, BigDecimal precioMax) {
        return this.productoRepository.findByPrecioBetween(precioMin, precioMax);
    }
    
    public Page<Producto> obtenerPorRangoPreciosPaginado(BigDecimal precioMin, BigDecimal precioMax, Pageable pageable) {
        return this.productoRepository.findByPrecioBetweenAndEstadoTrue(precioMin, precioMax, pageable);
    }
    
    public List<Producto> buscarProductos(String busqueda) {
        return this.productoRepository.buscarProductos(busqueda);
    }
    
    public Page<Producto> buscarProductosPaginado(String busqueda, Pageable pageable) {
        return this.productoRepository.buscarProductosPaginado(busqueda, pageable);
    }
    
    public List<Producto> obtenerProductosEcoAmigables() {
        return this.productoRepository.findProductosEcoAmigables(BigDecimal.valueOf(7.0));
    }
    
    public List<Producto> obtenerProductosConStock() {
        return this.productoRepository.findProductosConStock();
    }
    
    public List<Producto> obtenerProductosAgotados() {
        return this.productoRepository.findProductosAgotados();
    }
    
    public List<Producto> obtenerProductosConStockBajo() {
        return this.productoRepository.findProductosConStockBajo();
    }
    
    public List<Producto> obtenerProductosMasVendidos(int cantidad) {
        return this.productoRepository.findProductosMasVendidos(Pageable.ofSize(cantidad));
    }
    
    public List<Producto> obtenerProductosRecomendados(BigDecimal precioMaximo) {
        return this.productoRepository.findProductosRecomendados(precioMaximo);
    }
    
    // Métodos de creación y actualización
    public Producto crearProducto(String nombre, String descripcion, BigDecimal precio, String categoria,
                                 String marca, String modelo, String color, BigDecimal peso, 
                                 String dimensiones, String material, Integer garantiaMeses,
                                 String eficienciaEnergetica, String impactoAmbiental, 
                                 BigDecimal puntuacionEco, String imagenUrl, Integer stockInicial) {
        
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setCategoria(categoria);
        producto.setMarca(marca);
        producto.setModelo(modelo);
        producto.setColor(color);
        producto.setPeso(peso);
        producto.setDimensiones(dimensiones);
        producto.setMaterial(material);
        producto.setGarantiaMeses(garantiaMeses);
        producto.setEficienciaEnergetica(eficienciaEnergetica);
        producto.setImpactoAmbiental(impactoAmbiental);
        producto.setPuntuacionEco(puntuacionEco);
        producto.setImagenUrl(imagenUrl);
        producto.setEstado(true);
        producto.setFechaCreacion(LocalDateTime.now());
        
        Producto productoGuardado = this.productoRepository.save(producto);
        
        // Crear inventario inicial
        if (stockInicial != null && stockInicial > 0) {
            this.crearInventarioInicial(productoGuardado, stockInicial);
        }
        
        return productoGuardado;
    }
    
    private void crearInventarioInicial(Producto producto, Integer stockInicial) {
        Inventario inventario = new Inventario();
        inventario.setProducto(producto);
        inventario.setStock(stockInicial);
        inventario.setStockMinimo(5); // Valor por defecto
        inventario.setEstado(true);
        inventario.setFechaActualizacion(LocalDateTime.now());
        inventario.setUsuarioActualizacion("SISTEMA");
        
        this.inventarioRepository.save(inventario);
    }
    
    public Producto actualizarProducto(Integer id, String nombre, String descripcion, BigDecimal precio,
                                      String categoria, String marca, String modelo, String color,
                                      BigDecimal peso, String dimensiones, String material,
                                      Integer garantiaMeses, String eficienciaEnergetica,
                                      String impactoAmbiental, BigDecimal puntuacionEco, String imagenUrl) {
        
        Optional<Producto> productoExistente = this.productoRepository.findById(id);
        if (productoExistente.isEmpty()) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
        
        Producto producto = productoExistente.get();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setCategoria(categoria);
        producto.setMarca(marca);
        producto.setModelo(modelo);
        producto.setColor(color);
        producto.setPeso(peso);
        producto.setDimensiones(dimensiones);
        producto.setMaterial(material);
        producto.setGarantiaMeses(garantiaMeses);
        producto.setEficienciaEnergetica(eficienciaEnergetica);
        producto.setImpactoAmbiental(impactoAmbiental);
        producto.setPuntuacionEco(puntuacionEco);
        producto.setImagenUrl(imagenUrl);
        producto.setFechaActualizacion(LocalDateTime.now());
        
        return this.productoRepository.save(producto);
    }
    
    public void desactivarProducto(Integer id) {
        Optional<Producto> productoExistente = this.productoRepository.findById(id);
        if (productoExistente.isEmpty()) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
        
        Producto producto = productoExistente.get();
        producto.setEstado(false);
        producto.setFechaActualizacion(LocalDateTime.now());
        this.productoRepository.save(producto);
    }
    
    public void activarProducto(Integer id) {
        Optional<Producto> productoExistente = this.productoRepository.findById(id);
        if (productoExistente.isEmpty()) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
        
        Producto producto = productoExistente.get();
        producto.setEstado(true);
        producto.setFechaActualizacion(LocalDateTime.now());
        this.productoRepository.save(producto);
    }
    
    // Métodos de consulta
    public List<String> obtenerCategorias() {
        return this.productoRepository.findCategorias();
    }
    
    public List<String> obtenerMarcas() {
        return this.productoRepository.findMarcas();
    }
    
    // Estadísticas
    public Long contarProductosActivos() {
        return this.productoRepository.contarProductosActivos();
    }
    
    public Long contarPorCategoria(String categoria) {
        return this.productoRepository.contarPorCategoria(categoria);
    }
    
    public BigDecimal obtenerPrecioPromedio() {
        return this.productoRepository.obtenerPrecioPromedio();
    }
    
    public BigDecimal obtenerPuntuacionEcoPromedio() {
        return this.productoRepository.obtenerPuntuacionEcoPromedio();
    }
    
    // Validaciones
    public boolean tieneStock(Integer idProducto) {
        Optional<Producto> producto = this.productoRepository.findById(idProducto);
        return producto.isPresent() && producto.get().tieneStock();
    }
    
    public Integer obtenerStockDisponible(Integer idProducto) {
        Optional<Producto> producto = this.productoRepository.findById(idProducto);
        return producto.map(Producto::getStockDisponible).orElse(0);
    }
    
    public boolean puedeEliminarProducto(Integer id) {
        Optional<Producto> producto = this.productoRepository.findById(id);
        if (producto.isEmpty()) {
            return false;
        }
        
        // No se puede eliminar si tiene pedidos asociados
        return producto.get().getPedidoDetalles() == null || producto.get().getPedidoDetalles().isEmpty();
    }
    
    // Métodos adicionales para paginación y categorías
    public Page<Producto> obtenerProductosPaginados(Pageable pageable) {
        return this.productoRepository.findByEstadoTrue(pageable);
    }
    
    public List<String> obtenerCategoriasDisponibles() {
        return this.productoRepository.findCategorias();
    }
    
    public boolean validarSKUUnico(String sku, Integer idProducto) {
        // Como no tenemos campo SKU, validamos por nombre
        List<Producto> productos = this.productoRepository.findByNombre(sku);
        
        if (productos.isEmpty()) {
            return true;
        }
        
        // Si existe un producto con el mismo nombre, verificar si es el mismo que estamos editando
        if (idProducto != null) {
            return productos.stream().allMatch(p -> p.getIdProducto().equals(idProducto));
        }
        
        return false;
    }
    
    public void crearInventarioInicial(Producto producto) {
        if (producto.getInventario() == null) {
            Inventario inventario = new Inventario();
            inventario.setProducto(producto);
            inventario.setStock(0);
            inventario.setStockMinimo(5);
            inventario.setStockMaximo(100);
            inventario.setFechaActualizacion(LocalDateTime.now());
            
            this.inventarioRepository.save(inventario);
        }
    }
}
