package com.ecomaxtienda.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecomaxtienda.entity.Pedido;
import com.ecomaxtienda.entity.Usuario;
import com.ecomaxtienda.service.ExportService;
import com.ecomaxtienda.service.PedidoService;
import com.ecomaxtienda.service.UsuarioService;

@Controller
@RequestMapping("/admin")
public class PedidoAdminController extends BaseAdminController {
    
    private final PedidoService pedidoService;
    private final ExportService exportService;
    
    public PedidoAdminController(PedidoService pedidoService, UsuarioService usuarioService, ExportService exportService) {
        this.pedidoService = pedidoService;
        this.exportService = exportService;
    }
    
    /**
     * Página principal de gestión de pedidos
     */
    @GetMapping("/pedidos")
    public String gestionPedidos(Model model, Principal principal,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(defaultValue = "fechaPedido") String sortBy,
                                @RequestParam(defaultValue = "desc") String sortDir,
                                @RequestParam(required = false) String estado,
                                @RequestParam(required = false) String busqueda) {
        
        // Usuario actual para el header
        if (principal != null) {
            Usuario usuario = usuarioService.findByEmail(principal.getName());
            model.addAttribute("usuario", usuario);
        }
        
        // Configurar paginación y ordenamiento
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Obtener pedidos según filtros
        Page<Pedido> pedidos;
        if (estado != null && !estado.trim().isEmpty()) {
            pedidos = pedidoService.obtenerPedidosPorEstadoPaginado(estado, pageable);
        } else if (busqueda != null && !busqueda.trim().isEmpty()) {
            pedidos = pedidoService.buscarPedidosPaginado(busqueda, pageable);
        } else {
            pedidos = pedidoService.obtenerPedidosPaginados(pageable);
        }
        
        // Estadísticas
        long totalPedidos = pedidoService.contarPedidos();
        long pedidosPendientes = pedidoService.contarPedidosPorEstado("PENDIENTE");
        long pedidosEntregados = pedidoService.contarPedidosPorEstado("ENTREGADO");
        
        // Calcular ingresos totales (pedidos entregados)
        BigDecimal ingresosTotales = pedidoService.calcularIngresosTotales();
        
        // Agregar al modelo
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pedidos.getTotalPages());
        model.addAttribute("totalItems", pedidos.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("estado", estado);
        model.addAttribute("busqueda", busqueda);
        
        // Estadísticas
        model.addAttribute("totalPedidos", totalPedidos);
        model.addAttribute("pedidosPendientes", pedidosPendientes);
        model.addAttribute("pedidosEntregados", pedidosEntregados);
        model.addAttribute("ingresosTotales", ingresosTotales != null ? ingresosTotales.doubleValue() : 0.0);
        
        // Estados disponibles
        model.addAttribute("estados", List.of("PENDIENTE", "CONFIRMADO", "PREPARANDO", "ENVIADO", "ENTREGADO", "CANCELADO"));
        
        return "admin/pedidos";
    }
    
    /**
     * Ver detalles del pedido
     */
    @GetMapping("/pedidos/detalle/{id}")
    public String detallePedido(@PathVariable Integer id, Model model, Principal principal) {
        if (principal != null) {
            Usuario usuario = usuarioService.findByEmail(principal.getName());
            model.addAttribute("usuario", usuario);
        }
        
        Pedido pedido = pedidoService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        
        model.addAttribute("pedido", pedido);
        model.addAttribute("estados", List.of("PENDIENTE", "CONFIRMADO", "PREPARANDO", "ENVIADO", "ENTREGADO", "CANCELADO"));
        
        return "admin/pedido-detalle";
    }
    
    /**
     * Cambiar estado del pedido
     */
    @PostMapping("/pedidos/cambiar-estado/{id}")
    public String cambiarEstadoPedido(@PathVariable Integer id,
                                     @RequestParam String nuevoEstado,
                                     RedirectAttributes redirectAttributes) {
        try {
            pedidoService.cambiarEstadoPedido(id, nuevoEstado);
            redirectAttributes.addFlashAttribute("success", 
                "Estado del pedido actualizado a: " + nuevoEstado);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error al cambiar estado del pedido: " + e.getMessage());
        }
        
        return "redirect:/admin/pedidos/detalle/" + id;
    }
    
    /**
     * Cancelar pedido
     */
    @PostMapping("/pedidos/cancelar/{id}")
    public String cancelarPedido(@PathVariable Integer id,
                                @RequestParam(required = false) String motivo,
                                RedirectAttributes redirectAttributes) {
        try {
            pedidoService.cancelarPedido(id, motivo);
            redirectAttributes.addFlashAttribute("success", 
                "Pedido cancelado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error al cancelar pedido: " + e.getMessage());
        }
        
        return "redirect:/admin/pedidos";
    }
    
    /**
     * API: Obtener estadísticas de pedidos
     */
    @GetMapping("/pedidos/api/estadisticas")
    @ResponseBody
    public Object obtenerEstadisticasPedidos() {
        return new Object() {
            public final long confirmados = pedidoService.contarPedidosPorEstado("CONFIRMADO");
            public final long enviados = pedidoService.contarPedidosPorEstado("ENVIADO");
            public final long entregados = pedidoService.contarPedidosPorEstado("ENTREGADO");
            public final long cancelados = pedidoService.contarPedidosPorEstado("CANCELADO");
        };
    }
    
    /**
     * Exportar pedidos a PDF
     */
    @GetMapping("/pedidos/exportar/pdf")
    @SuppressWarnings("UseSpecificCatch")
    public ResponseEntity<byte[]> exportarPedidosPDF(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(defaultValue = "todos") String estados) {
        
        try {
            List<Pedido> pedidos = obtenerPedidosParaExportacion(fechaInicio, fechaFin, estados);
            byte[] pdfBytes = exportService.exportarPedidosPDF(pedidos, fechaInicio, fechaFin, estados);
            
            String filename = "pedidos_" + LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".pdf";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Exportar pedidos a Excel
     */
    @GetMapping("/pedidos/exportar/excel")
    @SuppressWarnings("UseSpecificCatch")
    public ResponseEntity<byte[]> exportarPedidosExcel(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(defaultValue = "todos") String estados) {
        
        try {
            List<Pedido> pedidos = obtenerPedidosParaExportacion(fechaInicio, fechaFin, estados);
            byte[] excelBytes = exportService.exportarPedidosExcel(pedidos);
            
            String filename = "pedidos_" + LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelBytes);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Exportar pedidos a CSV
     */
    @GetMapping("/pedidos/exportar/csv")
    @SuppressWarnings("UseSpecificCatch")
    public ResponseEntity<byte[]> exportarPedidosCSV(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(defaultValue = "todos") String estados) {
        
        try {
            List<Pedido> pedidos = obtenerPedidosParaExportacion(fechaInicio, fechaFin, estados);
            byte[] csvBytes = exportService.exportarPedidosCSV(pedidos);
            
            String filename = "pedidos_" + LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".csv";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(csvBytes);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Método auxiliar para obtener pedidos según filtros
     */
    private List<Pedido> obtenerPedidosParaExportacion(String fechaInicio, String fechaFin, String estados) {
        LocalDateTime inicio = null;
        LocalDateTime fin = null;
        
        if (fechaInicio != null && !fechaInicio.trim().isEmpty()) {
            inicio = LocalDate.parse(fechaInicio, DateTimeFormatter.ofPattern("dd/MM/yyyy")).atStartOfDay();
        }
        
        if (fechaFin != null && !fechaFin.trim().isEmpty()) {
            fin = LocalDate.parse(fechaFin, DateTimeFormatter.ofPattern("dd/MM/yyyy")).atTime(23, 59, 59);
        }
        
        // Obtener pedidos según filtros
        if (inicio != null && fin != null) {
            return pedidoService.obtenerPedidosEntreFechas(inicio, fin);
        } else if (estados != null && !estados.equals("todos")) {
            return pedidoService.obtenerPedidosPorEstado(estados);
        } else {
            return pedidoService.findAll();
        }
    }
    
    /**
     * Página de reportes
     */
    @GetMapping("/reportes")
    public String reportes(Model model, Principal principal) {
        if (principal != null) {
            Usuario usuario = usuarioService.findByEmail(principal.getName());
            model.addAttribute("usuario", usuario);
        }
        
        // Estadísticas para el reporte
        long totalPedidos = pedidoService.contarPedidos();
        long pedidosPendientes = pedidoService.contarPedidosPorEstado("PENDIENTE");
        long pedidosEntregados = pedidoService.contarPedidosPorEstado("ENTREGADO");
        BigDecimal ingresosTotales = pedidoService.calcularIngresosTotales();
        
        model.addAttribute("totalPedidos", totalPedidos);
        model.addAttribute("pedidosPendientes", pedidosPendientes);
        model.addAttribute("pedidosEntregados", pedidosEntregados);
        model.addAttribute("ingresosTotales", ingresosTotales != null ? ingresosTotales.doubleValue() : 0.0);
        
        return "admin/reportes";
    }
    
    /**
     * Exportar reporte general a PDF
     */
    @GetMapping("/reportes/exportar/pdf")
    @SuppressWarnings("UseSpecificCatch")
    public ResponseEntity<byte[]> exportarReportePDF() {
        try {
            List<Pedido> pedidos = pedidoService.findAll();
            byte[] pdfBytes = exportService.exportarReporteGeneralPDF(pedidos);
            
            String filename = "reporte_general_" + LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".pdf";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Exportar reporte general a Excel
     */
    @GetMapping("/reportes/exportar/excel")
    @SuppressWarnings("UseSpecificCatch")
    public ResponseEntity<byte[]> exportarReporteExcel() {
        try {
            List<Pedido> pedidos = pedidoService.findAll();
            byte[] excelBytes = exportService.exportarReporteGeneralExcel(pedidos);
            
            String filename = "reporte_general_" + LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelBytes);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
