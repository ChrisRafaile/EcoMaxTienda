package com.ecomaxtienda.service;

import com.ecomaxtienda.entity.*;
import com.ecomaxtienda.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final PedidoDetalleRepository pedidoDetalleRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final InventarioService inventarioService;
    private final EmailService emailService;

    // Métodos CRUD básicos
    public List<Pedido> findAll() {
        return this.pedidoRepository.findAll();
    }
    
    public Optional<Pedido> findById(Integer id) {
        return this.pedidoRepository.findById(id);
    }
    
    public Optional<Pedido> findByNumeroPedido(String numeroPedido) {
        return this.pedidoRepository.findByNumeroPedido(numeroPedido);
    }
    
    public Pedido save(Pedido pedido) {
        return this.pedidoRepository.save(pedido);
    }
    
    // Métodos de negocio
    public List<Pedido> obtenerPedidosPorUsuario(Integer idUsuario) {
        Optional<Usuario> usuario = this.usuarioRepository.findById(idUsuario);
        if (usuario.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + idUsuario);
        }
        return this.pedidoRepository.findByUsuario(usuario.get());
    }
    
    public Page<Pedido> obtenerPedidosPorUsuarioPaginado(Integer idUsuario, Pageable pageable) {
        Optional<Usuario> usuario = this.usuarioRepository.findById(idUsuario);
        if (usuario.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + idUsuario);
        }
        return this.pedidoRepository.findByUsuario(usuario.get(), pageable);
    }
    
    public List<Pedido> obtenerPorEstado(String estado) {
        return this.pedidoRepository.findByEstado(estado);
    }
    
    public Page<Pedido> obtenerPorEstadoPaginado(String estado, Pageable pageable) {
        return this.pedidoRepository.findByEstado(estado, pageable);
    }
    
    public List<Pedido> obtenerPedidosRecientes(int cantidad) {
        return this.pedidoRepository.findPedidosRecientes(Pageable.ofSize(cantidad));
    }
    
    public List<Pedido> obtenerPedidosPendientes() {
        return this.pedidoRepository.findByEstadoOrderByFechaPedidoDesc("PENDIENTE");
    }
    
    public List<Pedido> buscarPedidos(String busqueda) {
        return this.pedidoRepository.buscarPedidos(busqueda);
    }
    
    // Crear pedido
    public Pedido crearPedido(Integer idUsuario, String direccionEnvio, String telefonoContacto, 
                             String metodoPago, String notas, List<ItemPedido> items) {
        
        Optional<Usuario> usuario = this.usuarioRepository.findById(idUsuario);
        if (usuario.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + idUsuario);
        }
        
        // Validar stock de productos
        for (ItemPedido item : items) {
            if (!this.inventarioService.verificarDisponibilidad(item.getIdProducto(), item.getCantidad())) {
                Optional<Producto> producto = this.productoRepository.findById(item.getIdProducto());
                String nombreProducto = producto.map(Producto::getNombre).orElse("Producto " + item.getIdProducto());
                throw new RuntimeException("Stock insuficiente para el producto: " + nombreProducto);
            }
        }
        
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario.get());
        pedido.setDireccionEnvio(direccionEnvio);
        pedido.setTelefonoContacto(telefonoContacto);
        pedido.setMetodoPago(metodoPago);
        pedido.setNotas(notas);
        pedido.setEstado("PENDIENTE");
        pedido.setFechaPedido(LocalDateTime.now());
        
        // Calcular fecha estimada de entrega (7 días por defecto)
        pedido.setFechaEstimadaEntrega(LocalDateTime.now().plusDays(7));
        
        Pedido pedidoGuardado = this.pedidoRepository.save(pedido);
        
        // Crear detalles del pedido
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemPedido item : items) {
            PedidoDetalle detalle = this.crearDetallePedido(pedidoGuardado, item);
            subtotal = subtotal.add(detalle.getSubtotal());
        }
        
        // Actualizar totales del pedido
        pedidoGuardado.setSubtotal(subtotal);
        pedidoGuardado.calcularTotales();
        
        pedidoGuardado = this.pedidoRepository.save(pedidoGuardado);
        
        // Reducir stock de inventario
        for (ItemPedido item : items) {
            this.inventarioService.reducirStock(item.getIdProducto(), item.getCantidad(), "Venta - Pedido " + pedidoGuardado.getNumeroPedido());
        }
          // Enviar email de confirmación
        try {
            this.emailService.enviarCorreoConfirmacionPedido(usuario.get().getEmail(), usuario.get().getNombreCompleto(), 
                                                pedidoGuardado.getNumeroPedido(), pedidoGuardado.getTotal().toString());
        } catch (Exception e) {
            System.err.println("Error enviando confirmación de pedido: " + e.getMessage());
        }
        
        return pedidoGuardado;
    }
    
    private PedidoDetalle crearDetallePedido(Pedido pedido, ItemPedido item) {
        Optional<Producto> producto = this.productoRepository.findById(item.getIdProducto());
        if (producto.isEmpty()) {
            throw new RuntimeException("Producto no encontrado con ID: " + item.getIdProducto());
        }
        
        PedidoDetalle detalle = new PedidoDetalle();
        detalle.setPedido(pedido);
        detalle.setProducto(producto.get());
        detalle.setCantidad(item.getCantidad());
        detalle.setPrecioUnitario(producto.get().getPrecio());
        detalle.setDescuentoUnitario(item.getDescuentoUnitario() != null ? item.getDescuentoUnitario() : BigDecimal.ZERO);
        detalle.calcularSubtotal();
        
        return this.pedidoDetalleRepository.save(detalle);
    }
    
    // Gestión de estados del pedido
    public void confirmarPedido(Integer idPedido) {
        Optional<Pedido> pedidoExistente = this.pedidoRepository.findById(idPedido);
        if (pedidoExistente.isEmpty()) {
            throw new RuntimeException("Pedido no encontrado con ID: " + idPedido);
        }
        
        Pedido pedido = pedidoExistente.get();
        pedido.confirmar();
        this.pedidoRepository.save(pedido);
    }
    
    public void enviarPedido(Integer idPedido, String numeroSeguimiento, String transportadora) {
        Optional<Pedido> pedidoExistente = this.pedidoRepository.findById(idPedido);
        if (pedidoExistente.isEmpty()) {
            throw new RuntimeException("Pedido no encontrado con ID: " + idPedido);
        }
        
        Pedido pedido = pedidoExistente.get();
        pedido.enviar(numeroSeguimiento, transportadora);
        this.pedidoRepository.save(pedido);
    }
    
    public void entregarPedido(Integer idPedido) {
        Optional<Pedido> pedidoExistente = this.pedidoRepository.findById(idPedido);
        if (pedidoExistente.isEmpty()) {
            throw new RuntimeException("Pedido no encontrado con ID: " + idPedido);
        }
        
        Pedido pedido = pedidoExistente.get();
        pedido.entregar();
        this.pedidoRepository.save(pedido);
    }
    
    public void cancelarPedido(Integer idPedido) {
        Optional<Pedido> pedidoExistente = this.pedidoRepository.findById(idPedido);
        if (pedidoExistente.isEmpty()) {
            throw new RuntimeException("Pedido no encontrado con ID: " + idPedido);
        }
        
        Pedido pedido = pedidoExistente.get();
        
        if (!pedido.puedeSerCancelado()) {
            throw new RuntimeException("El pedido no puede ser cancelado en su estado actual: " + pedido.getEstado());
        }
        
        pedido.cancelar();
        
        // Restaurar stock
        List<PedidoDetalle> detalles = this.pedidoDetalleRepository.findByPedido(pedido);
        for (PedidoDetalle detalle : detalles) {
            this.inventarioService.aumentarStock(detalle.getProducto().getIdProducto(), detalle.getCantidad(), 
                                          "Cancelación - Pedido " + pedido.getNumeroPedido());
        }
        
        this.pedidoRepository.save(pedido);
    }
    
    // Estadísticas
    public Long contarPorEstado(String estado) {
        return this.pedidoRepository.contarPorEstado(estado);
    }
    
    public BigDecimal calcularVentasEnPeriodo(LocalDateTime inicio, LocalDateTime fin) {
        return this.pedidoRepository.calcularVentasEnPeriodo(inicio, fin);
    }
    
    public BigDecimal obtenerTicketPromedio() {
        return this.pedidoRepository.obtenerTicketPromedio();
    }
    
    public List<String> obtenerEstados() {
        return this.pedidoRepository.findEstados();
    }
    
    public List<String> obtenerMetodosPago() {
        return this.pedidoRepository.findMetodosPago();
    }
    
    public List<Pedido> obtenerPedidosQueNecesitanAtencion() {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(3); // Pendientes por más de 3 días
        return this.pedidoRepository.findPedidosQueNecesitanAtencion(fechaLimite);
    }
    
    public List<Pedido> obtenerPedidosConRetraso() {
        return this.pedidoRepository.findPedidosConRetraso(LocalDateTime.now());
    }
    
    // Clase auxiliar para items del pedido
    public static class ItemPedido {
        private Integer idProducto;
        private Integer cantidad;
        private BigDecimal descuentoUnitario;
        
        // Constructores
        public ItemPedido() {}
        
        public ItemPedido(Integer idProducto, Integer cantidad) {
            this.idProducto = idProducto;
            this.cantidad = cantidad;
            this.descuentoUnitario = BigDecimal.ZERO;
        }
        
        public ItemPedido(Integer idProducto, Integer cantidad, BigDecimal descuentoUnitario) {
            this.idProducto = idProducto;
            this.cantidad = cantidad;
            this.descuentoUnitario = descuentoUnitario;
        }
        
        // Getters y setters
        public Integer getIdProducto() { return this.idProducto; }
        public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }
        
        public Integer getCantidad() { return this.cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
        
        public BigDecimal getDescuentoUnitario() { return this.descuentoUnitario; }
        public void setDescuentoUnitario(BigDecimal descuentoUnitario) { this.descuentoUnitario = descuentoUnitario; }
    }
}
