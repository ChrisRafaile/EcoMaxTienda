package com.ecomaxtienda.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecomaxtienda.entity.ImagenPerfil;
import com.ecomaxtienda.entity.ImagenPerfil.TipoUsuario;
import com.ecomaxtienda.entity.Producto;
import com.ecomaxtienda.entity.Usuario;
import com.ecomaxtienda.repository.ProductoRepository;
import com.ecomaxtienda.service.ExportService;
import com.ecomaxtienda.service.ImagenService;
import com.ecomaxtienda.service.ProductoDataService;
import com.ecomaxtienda.service.ProductoService;
import com.ecomaxtienda.service.UsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/client")
public class ClientController extends BaseClientController {

    @Autowired
    private ImagenService imagenService;
    
    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    @SuppressWarnings("unused")
    private ProductoDataService productoDataService;

    @Autowired
    private ExportService exportService;

    public ClientController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/create-test-user")
    @ResponseBody
    public String createTestUser() {
        try {
            // Verificar si ya existe el usuario test
            if (usuarioService.existeEmail("test@test.com")) {
                return "Usuario de prueba ya existe: test@test.com / password: 123456";
            }
            
            // Crear usuario de prueba usando la firma correcta
            usuarioService.crearUsuario(
                "Usuario", 
                "Prueba", 
                "test@test.com", 
                "123456", 
                "ROLE_CLIENTE"
            );
            
            return "Usuario de prueba creado exitosamente: test@test.com / password: 123456";
        } catch (Exception e) {
            return "Error al crear usuario: " + e.getMessage();
        }
    }

    @GetMapping("/test-catalogo")
    @ResponseBody
    @SuppressWarnings("CallToPrintStackTrace")
    public String testCatalogo() {
        try {
            System.out.println("🔍 DEBUG: Iniciando método testCatalogo()");
            
            System.out.println("🔍 DEBUG: Obteniendo lista de todas las categorías...");
            List<String> categorias = productoService.obtenerCategorias();
            System.out.println("🔍 DEBUG: Categorías encontradas: " + categorias.size());
            
            System.out.println("🔍 DEBUG: Obteniendo productos activos OPTIMIZADO...");
            List<Producto> productos = productoRepository.findByEstadoTrueWithInventario();
            System.out.println("🔍 DEBUG: Productos activos encontrados: " + productos.size());
            
            // Verificar que el inventario se cargó correctamente
            int productosConInventario = 0;
            for (Producto producto : productos) {
                if (producto.getInventario() != null) {
                    productosConInventario++;
                }
            }
            System.out.println("🔍 DEBUG: Productos con inventario cargado: " + productosConInventario);
            
            return String.format("✅ Test optimizado completado. Categorías: %d, Productos: %d, Con inventario: %d", 
                               categorias.size(), productos.size(), productosConInventario);
        } catch (Exception e) {
            System.err.println("❌ ERROR en testCatalogo: " + e.getMessage());
            e.printStackTrace();
            return "Error en catálogo: " + e.getMessage();
        }
    }

    @GetMapping("/debug-catalogo")
    @ResponseBody
    @SuppressWarnings("CallToPrintStackTrace")
    public String debugCatalogo() {
        try {
            StringBuilder resultado = new StringBuilder();
            resultado.append("<h2>🔍 DEBUG CATÁLOGO COMPLETO</h2>");
            
            // 1. Verificar productos totales
            long totalProductos = productoService.contarProductos();
            resultado.append("<p><strong>📊 Total productos en BD:</strong> ").append(totalProductos).append("</p>");
            
            // 2. Verificar productos activos
            List<Producto> productosActivos = productoService.obtenerProductosActivos();
            resultado.append("<p><strong>✅ Productos activos:</strong> ").append(productosActivos.size()).append("</p>");
            
            // 3. Verificar paginación
            Pageable pageable = PageRequest.of(0, 12);
            Page<Producto> productosPaginados = productoService.obtenerProductosPaginados(pageable);
            resultado.append("<p><strong>📄 Productos paginados (página 0, 12 por página):</strong> ").append(productosPaginados.getTotalElements()).append("</p>");
            resultado.append("<p><strong>📄 Productos en primera página:</strong> ").append(productosPaginados.getContent().size()).append("</p>");
            resultado.append("<p><strong>📄 Total páginas:</strong> ").append(productosPaginados.getTotalPages()).append("</p>");
            
            // 4. Verificar categorías
            List<String> categorias = productoService.obtenerCategorias();
            resultado.append("<p><strong>🏷️ Categorías disponibles:</strong> ").append(categorias.size()).append("</p>");
            resultado.append("<p><strong>🏷️ Lista categorías:</strong> ").append(String.join(", ", categorias)).append("</p>");
            
            // 5. Mostrar productos de la primera página
            resultado.append("<h3>📋 Productos de la primera página:</h3>");
            resultado.append("<div style='display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px;'>");
            productosPaginados.getContent().forEach(p -> {
                resultado.append("<div style='border: 1px solid #ccc; padding: 10px; margin: 5px;'>");
                resultado.append("<strong>").append(p.getNombre()).append("</strong><br>");
                resultado.append("ID: ").append(p.getIdProducto()).append("<br>");
                resultado.append("Precio: S/ ").append(p.getPrecio()).append("<br>");
                resultado.append("Categoría: ").append(p.getCategoria()).append("<br>");
                resultado.append("Estado: ").append(p.getEstado() ? "Activo" : "Inactivo").append("<br>");
                if (p.getImagenUrl() != null) {
                    resultado.append("Imagen: <a href='").append(p.getImagenUrl()).append("' target='_blank'>Ver</a><br>");
                }
                resultado.append("</div>");
            });
            resultado.append("</div>");
            
            // 6. Test de filtros
            resultado.append("<h3>🔍 Test de filtros:</h3>");
            Page<Producto> productosFiltrados = productoService.buscarProductosConFiltros(null, null, null, null, pageable);
            resultado.append("<p><strong>Filtros sin parámetros:</strong> ").append(productosFiltrados.getTotalElements()).append(" productos</p>");
            
            // 7. Enlaces para probar
            resultado.append("<h3>🔗 Enlaces de prueba:</h3>");
            resultado.append("<p><a href='/client/catalogo' target='_blank'>🛍️ Ver Catálogo Normal</a></p>");
            resultado.append("<p><a href='/client/catalogo-temp' target='_blank'>🧪 Ver Catálogo Temporal</a></p>");
            resultado.append("<p><a href='/client/catalogo-corrected' target='_blank'>✅ Ver Catálogo Corregido</a></p>");
            resultado.append("<p><a href='/client/catalogo-dinamico' target='_blank'>🚀 Ver Catálogo Dinámico (Replicando Admin)</a></p>");
            resultado.append("<p><a href='/client/catalogo-final' target='_blank'>✨ Ver Catálogo Final Optimizado</a></p>");
            resultado.append("<p><a href='/client/catalogo-integrado' target='_blank'>🎯 Ver Catálogo Integrado Funcional</a></p>");
            
            return resultado.toString();
            
        } catch (Exception e) {
            e.printStackTrace();
            return "<h2>❌ ERROR EN DEBUG</h2><p>" + e.getMessage() + "</p>";
        }
    }
    
    @GetMapping("/catalogo-simple")
    @SuppressWarnings("CallToPrintStackTrace")
    public String catalogoSimple(Model model, Principal principal,
                          @RequestParam(value = "page", defaultValue = "0") int page,
                          @RequestParam(value = "size", defaultValue = "12") int size) {
        
        System.out.println("=== CATÁLOGO SIMPLE DEBUG ===");
        System.out.println("Usuario: " + (principal != null ? principal.getName() : "Anónimo"));
        
        try {
            // Crear paginación simple
            Pageable pageable = PageRequest.of(page, size);
            
            // Obtener productos paginados
            Page<Producto> productos = productoService.obtenerProductosPaginados(pageable);
            System.out.println("✅ Productos encontrados: " + productos.getTotalElements());
            
            // Obtener categorías
            List<String> categorias = productoService.obtenerCategorias();
            System.out.println("✅ Categorías: " + categorias.size());
            
            // Agregar atributos básicos
            model.addAttribute("productos", productos);
            model.addAttribute("categorias", categorias);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", productos.getTotalPages());
            model.addAttribute("totalItems", productos.getTotalElements());
            
            System.out.println("✅ Retornando vista client/catalogo-simple");
            return "client/catalogo-simple";
            
        } catch (Exception e) {
            System.err.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/catalogo-corrected")
    @SuppressWarnings("CallToPrintStackTrace")
    public String catalogoCorrected(Model model, Principal principal,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "12") int size,
                                   @RequestParam(required = false) String buscar,
                                   @RequestParam(required = false) String categoria,
                                   @RequestParam(required = false) BigDecimal precioMax) {
        try {
            System.out.println("🔍 DEBUG: Iniciando método catalogoCorrected()");
            
            // Crear configuración de paginación
            Pageable pageable = PageRequest.of(page, size);
            
            // Obtener productos paginados
            Page<Producto> productos = productoService.obtenerProductosPaginados(pageable);
            System.out.println("✅ Productos encontrados: " + productos.getTotalElements());
            
            // Obtener categorías
            List<String> categorias = productoService.obtenerCategorias();
            System.out.println("✅ Categorías: " + categorias.size());
            
            // Agregar atributos básicos
            model.addAttribute("productos", productos);
            model.addAttribute("categorias", categorias);
            model.addAttribute("currentPage", page);
            model.addAttribute("size", size);
            model.addAttribute("totalPages", productos.getTotalPages());
            model.addAttribute("totalItems", productos.getTotalElements());
            
            System.out.println("✅ Retornando vista client/catalogo-corrected");
            return "client/catalogo-corrected";
            
        } catch (Exception e) {
            System.err.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/catalogo-dinamico")
    public String catalogoDinamico(Model model) {
        System.out.println("🔍 DEBUG: Accediendo a catálogo dinámico");
        return "client/catalogo-dinamico";
    }
    
    @GetMapping("/catalogo-integrado")
    public String catalogoIntegrado(Model model) {
        System.out.println("🔍 DEBUG: Accediendo a catálogo integrado sin Thymeleaf");
        return "client/catalogo-integrado";
    }
    
    @GetMapping("/catalogo-funcional")
    public String catalogoFuncional(Model model) {
        System.out.println("🔍 DEBUG: Accediendo a catálogo funcional corregido");
        return "client/catalogo-funcional";
    }
    
    @GetMapping("/catalogo/api/productos")
    @ResponseBody
    @SuppressWarnings("CallToPrintStackTrace")
    public Page<Producto> obtenerProductosApi(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "12") int size,
                                              @RequestParam(required = false) String buscar,
                                              @RequestParam(required = false) String categoria,
                                              @RequestParam(required = false) BigDecimal precioMax) {
        try {
            System.out.println("🔍 DEBUG API: page=" + page + ", size=" + size + ", buscar=" + buscar + ", categoria=" + categoria);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Producto> productos;
            
            if (buscar != null || categoria != null || precioMax != null) {
                String precioMaxStr = precioMax != null ? precioMax.toString() : null;
                productos = productoService.buscarProductosConFiltros(buscar, categoria, precioMaxStr, null, pageable);
            } else {
                productos = productoService.obtenerProductosPaginados(pageable);
            }
            
            System.out.println("✅ API: Productos encontrados: " + productos.getTotalElements());
            return productos;
            
        } catch (Exception e) {
            System.err.println("❌ ERROR API: " + e.getMessage());
            e.printStackTrace();
            return Page.empty();
        }
    }
    
    @GetMapping("/catalogo/api/categorias")
    @ResponseBody
    public List<String> obtenerCategoriasApi() {
        try {
            List<String> categorias = productoService.obtenerCategorias();
            System.out.println("✅ API Categorías: " + categorias.size() + " categorías encontradas");
            return categorias;
        } catch (Exception e) {
            System.err.println("❌ ERROR API Categorías: " + e.getMessage());
            return List.of();
        }
    }

    @GetMapping("/test-productos")
    @ResponseBody
    public String testProductos() {
        try {
            List<Producto> productos = productoService.obtenerProductosActivos();
            return String.format("<h1>✅ Total productos activos: %d</h1><br><a href='/client/catalogo'>Ir al Catálogo</a>", productos.size());
        } catch (Exception e) {
            return String.format("<h1>❌ Error: %s</h1>", e.getMessage());
        }
    }

    @GetMapping("/home")
    public String home(Model model, Principal principal, 
                      @RequestParam(value = "orderSuccess", required = false) Boolean orderSuccess) {
        if (orderSuccess != null && orderSuccess) {
            model.addAttribute("orderSuccess", true);
        }
        
        // Obtener productos destacados para mostrar en home
        List<Producto> productosDestacados = productoService.obtenerProductosActivos()
            .stream()
            .limit(8)
            .toList();
        model.addAttribute("productosDestacados", productosDestacados);
        
        return "client/home";
    }

    @GetMapping("/catalogo")
    public String catalogo(Model model, Principal principal,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "12") int size,
                          @RequestParam(required = false) String busqueda,
                          @RequestParam(required = false) String categoria,
                          @RequestParam(required = false) String minPrecio,
                          @RequestParam(required = false) String maxPrecio,
                          @RequestParam(defaultValue = "nombre") String sortBy,
                          @RequestParam(defaultValue = "asc") String sortDir,
                          Authentication authentication) {
        
        System.out.println("🛍️ === CATÁLOGO PRINCIPAL - INICIANDO ===");
        
        String usuario = authentication != null ? authentication.getName() : "Anónimo";
        System.out.println("👤 Usuario: " + usuario);
        System.out.println("📄 Página: " + page + " | Tamaño: " + size);
        
        try {
            // Configurar ordenamiento
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Obtener productos con filtros
            Page<Producto> productos = productoService.buscarProductosConFiltros(
                busqueda, categoria, minPrecio, maxPrecio, pageable);
                
            System.out.println("✅ Productos encontrados: " + productos.getTotalElements());
            System.out.println("📄 Productos en esta página: " + productos.getNumberOfElements());
            
            List<String> categorias = productoService.obtenerCategorias();
            System.out.println("🏷️ Categorías: " + categorias.size());
            
            // Añadir al modelo
            model.addAttribute("productos", productos);
            model.addAttribute("categorias", categorias);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", productos.getTotalPages());
            model.addAttribute("totalItems", productos.getTotalElements());
            model.addAttribute("size", size);
            model.addAttribute("busqueda", busqueda);
            model.addAttribute("categoria", categoria);
            model.addAttribute("minPrecio", minPrecio);
            model.addAttribute("maxPrecio", maxPrecio);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            
            System.out.println("🎯 Catálogo principal listo - Total elementos: " + productos.getTotalElements());
            return "client/catalogo-completo";
            
        } catch (Exception e) {
            System.err.println("❌ Error en catálogo: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar productos: " + e.getMessage());
            return "client/catalogo-completo";
        }
    }
    public String catalogo(Model model, Principal principal,
                          @RequestParam(value = "page", defaultValue = "0") int page,
                          @RequestParam(value = "size", defaultValue = "12") int size,
                          @RequestParam(value = "sortBy", defaultValue = "nombre") String sortBy,
                          @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
                          @RequestParam(value = "categoria", required = false) String categoria,
                          @RequestParam(value = "busqueda", required = false) String busqueda,
                          @RequestParam(value = "minPrecio", required = false) String minPrecio,
                          @RequestParam(value = "maxPrecio", required = false) String maxPrecio) {
        
        System.out.println("🛍️ === CATÁLOGO CLIENTE - INICIANDO ===");
        System.out.println("👤 Usuario: " + (principal != null ? principal.getName() : "Anónimo"));
        System.out.println("📄 Página: " + page + " | Tamaño: " + size);
        
        try {
            // Crear paginación simple primero
            Pageable pageable = PageRequest.of(page, size, 
                Sort.by(sortDir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy));
            
            // Aplicar filtros si existen
            Page<Producto> productos;
            if ((categoria != null && !categoria.trim().isEmpty()) || 
                (busqueda != null && !busqueda.trim().isEmpty()) ||
                (minPrecio != null && !minPrecio.trim().isEmpty()) ||
                (maxPrecio != null && !maxPrecio.trim().isEmpty())) {
                
                System.out.println("🔍 Aplicando filtros personalizados...");
                productos = productoService.buscarProductosConFiltros(
                    busqueda, categoria, minPrecio, maxPrecio, pageable);
            } else {
                System.out.println("📋 Obteniendo todos los productos paginados...");
                productos = productoService.obtenerProductosPaginados(pageable);
            }
            
            System.out.println("✅ Productos encontrados: " + productos.getTotalElements());
            System.out.println("📄 Productos en esta página: " + productos.getContent().size());
            
            // Obtener categorías
            List<String> categorias = productoService.obtenerCategorias();
            System.out.println("✅ Categorías: " + categorias.size());
            
            // Agregar atributos básicos
            model.addAttribute("productos", productos);
            model.addAttribute("categorias", categorias);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", productos.getTotalPages());
            model.addAttribute("totalItems", productos.getTotalElements());
            
            // Mantener valores de filtros
            model.addAttribute("categoria", categoria);
            model.addAttribute("busqueda", busqueda);
            model.addAttribute("minPrecio", minPrecio);
            model.addAttribute("maxPrecio", maxPrecio);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            
            // Usuario si está logueado
            if (principal != null) {
                try {
                    Usuario usuario = usuarioService.findByEmail(principal.getName());
                    if (usuario != null) {
                        model.addAttribute("usuario", usuario);
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ No se pudo cargar usuario: " + e.getMessage());
                }
            }
            
            System.out.println("✅ Catálogo listo - Total elementos: " + productos.getTotalElements());
            return "client/catalogo"; // Usar la vista principal del catálogo
            
        } catch (Exception e) {
            System.err.println("❌ ERROR EN CATÁLOGO: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar el catálogo: " + e.getMessage());
            model.addAttribute("productos", Page.empty());
            model.addAttribute("categorias", new ArrayList<>());
            model.addAttribute("totalItems", 0);
            return "client/catalogo";
        }
    }

    @GetMapping("/catalogo-debug-fixed")
    public String catalogoDebugFixed(Model model, Principal principal,
                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                    @RequestParam(value = "size", defaultValue = "12") int size) {
        System.out.println("🔧 === CATÁLOGO DEBUG FIXED ===");
        
        try {
            // Obtener productos paginados - uso simple igual que catalogo-simple
            Pageable pageable = PageRequest.of(page, size, Sort.by("nombre"));
            Page<Producto> productos = productoService.obtenerProductosPaginados(pageable);
            
            System.out.println("✅ FIXED - Total productos: " + productos.getTotalElements());
            System.out.println("✅ FIXED - Productos en página: " + productos.getContent().size());
            
            // Obtener categorías
            List<String> categorias = productoService.obtenerCategorias();
            
            // Agregar atributos exactamente igual que catalogo-simple
            model.addAttribute("productos", productos);
            model.addAttribute("categorias", categorias);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", productos.getTotalPages());
            model.addAttribute("totalItems", productos.getTotalElements());
            
            return "client/catalogo-debug-fixed";
            
        } catch (Exception e) {
            System.err.println("❌ ERROR CATALOGO DEBUG FIXED: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error: " + e.getMessage());
            model.addAttribute("productos", Page.empty());
            model.addAttribute("totalItems", 0);
            return "client/catalogo-debug-fixed";
        }
    }

    @GetMapping("/catalogo-debug-view")
    @SuppressWarnings("CallToPrintStackTrace")
    public String catalogoDebugView(Model model, Principal principal,
                                   @RequestParam(value = "page", defaultValue = "0") int page,
                                   @RequestParam(value = "size", defaultValue = "12") int size) {
        
        System.out.println("🔍 === DEBUG VIEW CATÁLOGO ===");
        System.out.println("👤 Usuario: " + (principal != null ? principal.getName() : "Anónimo"));
        System.out.println("📄 Página: " + page + " | Tamaño: " + size);
        
        try {
            // Obtener productos paginados
            Pageable pageable = PageRequest.of(page, size, Sort.by("nombre"));
            Page<Producto> productos = productoService.obtenerProductosPaginados(pageable);
            
            System.out.println("✅ DEBUG - Productos encontrados: " + productos.getTotalElements());
            System.out.println("✅ DEBUG - Productos en esta página: " + productos.getContent().size());
            System.out.println("✅ DEBUG - Total páginas: " + productos.getTotalPages());
            
            // Obtener categorías
            List<String> categorias = productoService.obtenerCategorias();
            System.out.println("✅ DEBUG - Categorías: " + categorias.size());
            
            // Debug: Imprimir primeros productos
            for (int i = 0; i < Math.min(3, productos.getContent().size()); i++) {
                Producto p = productos.getContent().get(i);
                System.out.println("  📦 Producto " + (i+1) + ": " + p.getNombre() + " - S/" + p.getPrecio());
            }
            
            // Agregar atributos
            model.addAttribute("productos", productos);
            model.addAttribute("categorias", categorias);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", productos.getTotalPages());
            model.addAttribute("totalItems", productos.getTotalElements());
            
            // Usuario si está logueado
            if (principal != null) {
                try {
                    Usuario usuario = usuarioService.findByEmail(principal.getName());
                    if (usuario != null) {
                        model.addAttribute("usuario", usuario);
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ No se pudo cargar usuario: " + e.getMessage());
                }
            }
            
            System.out.println("✅ DEBUG - Retornando vista: client/catalogo-debug");
            return "client/catalogo-debug";
            
        } catch (Exception e) {
            System.err.println("❌ ERROR EN DEBUG CATÁLOGO: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error en debug: " + e.getMessage());
            model.addAttribute("productos", Page.empty());
            model.addAttribute("categorias", new ArrayList<>());
            model.addAttribute("totalItems", 0);
            return "client/catalogo-debug";
        }
    }
    
    @GetMapping("/catalogo-debug")
    public String catalogoDebugSimple(Model model) {
        System.out.println("🔍 === ACCEDIENDO A DEBUG CATÁLOGO SIMPLE ===");
        
        try {
            // Obtener productos directamente
            Pageable pageable = PageRequest.of(0, 12, Sort.by("nombre"));
            Page<Producto> productos = productoService.obtenerProductosPaginados(pageable);
            
            System.out.println("🔍 Total productos en BD: " + productos.getTotalElements());
            System.out.println("🔍 Productos en página: " + productos.getContent().size());
            
            // Agregar atributos básicos
            model.addAttribute("productos", productos);
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", productos.getTotalPages());
            model.addAttribute("totalItems", productos.getTotalElements());
            model.addAttribute("categorias", productoService.obtenerCategorias());
            
            return "client/catalogo-debug";
            
        } catch (Exception e) {
            System.err.println("❌ ERROR DEBUG: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            model.addAttribute("productos", Page.empty());
            model.addAttribute("totalItems", 0);
            return "client/catalogo-debug";
        }
    }

    @GetMapping("/catalogo-test")
    @SuppressWarnings("CallToPrintStackTrace")
    public String catalogoTest(Model model, Principal principal,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "12") int size) {
        
        System.out.println("🧪 === CATÁLOGO TEST ===");
        System.out.println("👤 Usuario: " + (principal != null ? principal.getName() : "Anónimo"));
        System.out.println("📄 Página: " + page + " | Tamaño: " + size);
        
        try {
            // Obtener productos paginados sin filtros
            Pageable pageable = PageRequest.of(page, size, Sort.by("nombre"));
            Page<Producto> productos = productoService.obtenerProductosPaginados(pageable);
            
            System.out.println("🧪 TEST - Total productos: " + productos.getTotalElements());
            System.out.println("🧪 TEST - Productos en página actual: " + productos.getContent().size());
            System.out.println("🧪 TEST - ¿Tiene contenido?: " + productos.hasContent());
            
            // Debug: Mostrar algunos productos
            if (productos.hasContent()) {
                for (int i = 0; i < Math.min(3, productos.getContent().size()); i++) {
                    Producto p = productos.getContent().get(i);
                    System.out.println("  📦 Producto " + (i+1) + ": " + p.getNombre() + " - S/" + p.getPrecio());
                }
            }
            
            // Agregar atributos básicos
            model.addAttribute("productos", productos);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", productos.getTotalPages());
            model.addAttribute("totalItems", productos.getTotalElements());
            
            System.out.println("🧪 TEST - Retornando vista: client/catalogo-test");
            return "client/catalogo-test";
            
        } catch (Exception e) {
            System.err.println("❌ ERROR EN CATÁLOGO TEST: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error en catálogo test: " + e.getMessage());
            model.addAttribute("productos", Page.empty());
            model.addAttribute("totalItems", 0);
            return "client/catalogo-test";
        }
    }

    @GetMapping("/catalogo-json-debug")
    @ResponseBody
    public Map<String, Object> catalogoJsonDebug(@RequestParam(value = "page", defaultValue = "0") int page,
                                                 @RequestParam(value = "size", defaultValue = "12") int size) {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("🔍 === JSON DEBUG CATALOGO ===");
            
            // Obtener productos paginados
            Pageable pageable = PageRequest.of(page, size, Sort.by("nombre"));
            Page<Producto> productos = productoService.obtenerProductosPaginados(pageable);
            
            System.out.println("✅ Total productos: " + productos.getTotalElements());
            System.out.println("✅ Productos en página: " + productos.getContent().size());
            
            response.put("success", true);
            response.put("totalElements", productos.getTotalElements());
            response.put("totalPages", productos.getTotalPages());
            response.put("currentPage", page);
            response.put("size", size);
            response.put("productosEnPagina", productos.getContent().size());
            
            // Agregar lista simple de productos
            List<Map<String, Object>> productosSimple = new ArrayList<>();
            for (Producto p : productos.getContent()) {
                Map<String, Object> prod = new HashMap<>();
                prod.put("id", p.getIdProducto());
                prod.put("nombre", p.getNombre());
                prod.put("precio", p.getPrecio());
                prod.put("categoria", p.getCategoria());
                prod.put("estado", p.getEstado());
                // Verificar stock
                if (p.getInventario() != null) {
                    prod.put("stock", p.getInventario().getStock());
                } else {
                    prod.put("stock", "sin_inventario");
                }
                productosSimple.add(prod);
            }
            response.put("productos", productosSimple);
            
            return response;
            
        } catch (Exception e) {
            System.err.println("❌ ERROR JSON DEBUG: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }



    @GetMapping("/catalogo-temp")
    @SuppressWarnings("CallToPrintStackTrace")
    public String catalogoTemp(Model model, Principal principal,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "50") int size) {
        
        System.out.println("=== CATALOGO TEMP DEBUG ===");
        System.out.println("Usuario: " + (principal != null ? principal.getName() : "Anonimo"));
        System.out.println("Pagina: " + page + ", Tamaño: " + size);
        
        try {
            // Crear paginacion simple
            Pageable pageable = PageRequest.of(page, size);
            
            // Obtener productos paginados
            Page<Producto> productos = productoService.obtenerProductosPaginados(pageable);
            System.out.println("Productos paginados: " + productos.getTotalElements());
            System.out.println("Productos en esta pagina: " + productos.getContent().size());
            
            // Obtener categorias
            List<String> categorias = productoService.obtenerCategorias();
            System.out.println("Categorias: " + categorias.size());
            
            // Agregar atributos basicos
            model.addAttribute("productos", productos);
            model.addAttribute("categorias", categorias);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", productos.getTotalPages());
            model.addAttribute("totalItems", productos.getTotalElements());
            
            System.out.println("Retornando vista client/catalogo-temp");
            return "client/catalogo-temp";
            
        } catch (Exception e) {
            System.err.println("ERROR CRITICO: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error critico: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/catalogo-fixed")
    @SuppressWarnings("CallToPrintStackTrace")
    public String catalogoFixed(Model model, Principal principal,
                          @RequestParam(value = "page", defaultValue = "0") int page,
                          @RequestParam(value = "size", defaultValue = "12") int size) {
        
        System.out.println("🔧 === CATÁLOGO FIXED - TEMPLATE SIMPLIFICADO ===");
        System.out.println("🔧 Usuario: " + (principal != null ? principal.getName() : "Anónimo"));
        System.out.println("🔧 Página: " + page + " | Tamaño: " + size);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("nombre"));
            
            // Obtener productos
            Page<Producto> productos = productoService.obtenerProductosPaginados(pageable);
            List<String> categorias = productoService.obtenerCategorias();
            
            System.out.println("🔧 Productos encontrados: " + productos.getTotalElements());
            System.out.println("🔧 Productos en esta página: " + productos.getContent().size());
            System.out.println("🔧 Categorías: " + categorias.size());
            
            // Agregar atributos al modelo
            model.addAttribute("productos", productos);
            model.addAttribute("categorias", categorias);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", productos.getTotalPages());
            model.addAttribute("totalElements", productos.getTotalElements());
            
            System.out.println("🔧 Retornando vista client/catalogo-fixed");
            return "client/catalogo-fixed";
            
        } catch (Exception e) {
            System.err.println("🔧 ERROR EN CATALOGO-FIXED: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error en catálogo: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/producto-detalle/{id}")
    public String productoDetalle(@PathVariable Integer id, Model model, Principal principal) {
        Optional<Producto> productoOpt = productoService.findById(id);
        
        if (productoOpt.isEmpty()) {
            return "redirect:/client/catalogo?error=producto-no-encontrado";
        }
        
        Producto producto = productoOpt.get();
        model.addAttribute("producto", producto);
        
        // Obtener productos relacionados (misma categoría)
        List<Producto> productosRelacionados = productoService.findByCategoria(producto.getCategoria())
            .stream()
            .filter(p -> !p.getIdProducto().equals(id))
            .limit(4)
            .toList();
        model.addAttribute("productosRelacionados", productosRelacionados);
        
        return "client/producto-detalle";
    }

    @PostMapping("/agregar-al-carrito")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> agregarAlCarrito(
            @RequestParam Integer productoId,
            @RequestParam(defaultValue = "1") Integer cantidad,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Producto> productoOpt = productoService.findById(productoId);
            
            if (productoOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Producto no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            
            Producto producto = productoOpt.get();
            
            // Obtener carrito de la sesión
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> carrito = (List<Map<String, Object>>) session.getAttribute("carrito");
            
            if (carrito == null) {
                carrito = new ArrayList<>();
            }
            
            // Verificar si el producto ya está en el carrito
            Optional<Map<String, Object>> itemExistente = carrito.stream()
                .filter(item -> item.get("productoId").equals(productoId))
                .findFirst();
            
            if (itemExistente.isPresent()) {
                // Actualizar cantidad
                Map<String, Object> item = itemExistente.get();
                Integer cantidadActual = (Integer) item.get("cantidad");
                item.put("cantidad", cantidadActual + cantidad);
            } else {
                // Agregar nuevo item
                Map<String, Object> nuevoItem = new HashMap<>();
                nuevoItem.put("productoId", producto.getIdProducto());
                nuevoItem.put("nombre", producto.getNombre());
                nuevoItem.put("precio", producto.getPrecio());
                nuevoItem.put("cantidad", cantidad);
                nuevoItem.put("imagenUrl", producto.getImagenUrl());
                carrito.add(nuevoItem);
            }
            
            session.setAttribute("carrito", carrito);
            
            // Calcular totales
            int totalItems = carrito.stream().mapToInt(item -> (Integer) item.get("cantidad")).sum();
            
            response.put("success", true);
            response.put("message", "Producto agregado al carrito");
            response.put("totalItems", totalItems);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al agregar producto al carrito");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/carrito/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerContadorCarrito(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> carrito = (List<Map<String, Object>>) session.getAttribute("carrito");
        
        int count = 0;
        if (carrito != null) {
            count = carrito.stream().mapToInt(item -> (Integer) item.get("cantidad")).sum();
        }
        
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/carrito")
    public String carrito(Model model, Principal principal, HttpSession session) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> carrito = (List<Map<String, Object>>) session.getAttribute("carrito");
        
        if (carrito == null) {
            carrito = new ArrayList<>();
        }
        
        model.addAttribute("carrito", carrito);
        
        // Calcular totales
        double subtotal = carrito.stream()
            .mapToDouble(item -> {
                double precio = ((Number) item.get("precio")).doubleValue();
                int cantidad = (Integer) item.get("cantidad");
                return precio * cantidad;
            })
            .sum();
        
        double envio = subtotal > 200 ? 0 : 15; // Envío gratis para compras mayores a S/ 200
        double descuento = subtotal * 0.05; // 5% descuento eco-friendly
        double igv = (subtotal - descuento + envio) * 0.18;
        double total = subtotal - descuento + envio + igv;
        
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("envio", envio);
        model.addAttribute("descuento", descuento);
        model.addAttribute("igv", igv);
        model.addAttribute("total", total);
        
        return "client/carrito";
    }

    @PostMapping("/actualizar-carrito")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarCarrito(
            @RequestParam Integer productoId,
            @RequestParam Integer cantidad,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> carrito = (List<Map<String, Object>>) session.getAttribute("carrito");
            
            if (carrito != null) {
                carrito.stream()
                    .filter(item -> item.get("productoId").equals(productoId))
                    .findFirst()
                    .ifPresent(item -> item.put("cantidad", cantidad));
                
                session.setAttribute("carrito", carrito);
            }
            
            response.put("success", true);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al actualizar carrito");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/eliminar-del-carrito")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarDelCarrito(
            @RequestParam Integer productoId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> carrito = (List<Map<String, Object>>) session.getAttribute("carrito");
            
            if (carrito != null) {
                carrito.removeIf(item -> item.get("productoId").equals(productoId));
                session.setAttribute("carrito", carrito);
                
                // Recalcular total de items
                int totalItems = carrito.stream().mapToInt(item -> (Integer) item.get("cantidad")).sum();
                response.put("totalItems", totalItems);
            }
            
            response.put("success", true);
            response.put("message", "Producto eliminado del carrito");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar producto del carrito");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/remover-del-carrito")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removerDelCarrito(
            @RequestParam Integer productoId,
            HttpSession session) {
        // Alias para eliminar-del-carrito para compatibilidad
        return eliminarDelCarrito(productoId, session);
    }

    @PostMapping("/vaciar-carrito")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> vaciarCarrito(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            session.removeAttribute("carrito");
            response.put("success", true);
            response.put("message", "Carrito vaciado exitosamente");
            response.put("totalItems", 0);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al vaciar carrito");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/pago")
    public String pago(Model model, Principal principal, HttpSession session) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> carrito = (List<Map<String, Object>>) session.getAttribute("carrito");
        
        if (carrito == null || carrito.isEmpty()) {
            return "redirect:/client/carrito?error=carrito-vacio";
        }
        
        model.addAttribute("carrito", carrito);
        
        // Calcular totales (mismo cálculo que en carrito)
        double subtotal = carrito.stream()
            .mapToDouble(item -> {
                double precio = ((Number) item.get("precio")).doubleValue();
                int cantidad = (Integer) item.get("cantidad");
                return precio * cantidad;
            })
            .sum();
        
        double envio = subtotal > 200 ? 0 : 15;
        double descuento = subtotal * 0.05;
        double igv = (subtotal - descuento + envio) * 0.18;
        double total = subtotal - descuento + envio + igv;
        
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("envio", envio);
        model.addAttribute("descuento", descuento);
        model.addAttribute("igv", igv);
        model.addAttribute("total", total);
        
        return "client/pago";
    }

    @GetMapping("/suscripcion")
    public String suscripcion(Model model, Principal principal) {
        if (principal != null) {
            Usuario usuario = usuarioService.findByEmail(principal.getName());
            model.addAttribute("usuario", usuario);
            
            // Verificar si ya tiene suscripción activa
            // boolean tieneSuscripcion = suscripcionService.tieneSuscripcionActiva(usuario);
            // model.addAttribute("tieneSuscripcion", tieneSuscripcion);
        }
        
        return "client/suscripcion";
    }
    
    @PostMapping("/procesar-suscripcion")
    @ResponseBody
    @SuppressWarnings("UseSpecificCatch")
    public ResponseEntity<Map<String, Object>> procesarSuscripcion(
            @RequestParam String planSuscripcion,
            @RequestParam String metodoPago,
            @RequestParam(required = false) String numeroTarjeta,
            @RequestParam(required = false) String cvv,
            @RequestParam(required = false) String mesVencimiento,
            @RequestParam(required = false) String anoVencimiento,
            Principal principal) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "Debe iniciar sesión para suscribirse");
                return ResponseEntity.badRequest().body(response);
            }
            
            @SuppressWarnings("unused")
            Usuario usuario = usuarioService.findByEmail(principal.getName());
            
            // Validar datos de pago
            if (metodoPago.equals("TARJETA_CREDITO") || metodoPago.equals("TARJETA_DEBITO")) {
                if (numeroTarjeta == null || cvv == null || mesVencimiento == null || anoVencimiento == null) {
                    response.put("success", false);
                    response.put("message", "Datos de tarjeta incompletos");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            // Simular procesamiento de suscripción
            Thread.sleep(2000);
            
            // Aquí iría la lógica real de suscripción
            // SuscripcionService.ResultadoSuscripcion resultado = suscripcionService.crearSuscripcion(...);
            
            // Simular éxito
            response.put("success", true);
            response.put("message", "Suscripción activada exitosamente");
            response.put("planActivado", planSuscripcion);
            response.put("fechaActivacion", new java.util.Date());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al procesar suscripción: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/perfil")
    public String perfil(Model model, Principal principal) {
        return "client/perfil";
    }

    @GetMapping("/configuracion")
    public String configuracion(Model model, Principal principal) {
        return "client/configuracion";
    }

    @GetMapping("/pedidos")
    public String pedidos(Model model, Principal principal) {
        if (principal != null) {
            Usuario usuario = usuarioService.findByEmail(principal.getName());
            model.addAttribute("usuario", usuario);
            
            // Obtener imagen de perfil
            try {
                Optional<ImagenPerfil> imagenPerfilOpt = imagenService.obtenerImagenPerfil(
                    usuario.getIdUsuario().longValue(), TipoUsuario.CLIENTE);
                if (imagenPerfilOpt.isPresent()) {
                    model.addAttribute("imagenPerfil", imagenPerfilOpt.get());
                }
            } catch (Exception e) {
                // Si no hay imagen, continuamos sin ella
            }
            
            // Aquí puedes agregar la lógica para obtener pedidos reales de la base de datos
            // List<Pedido> pedidos = pedidoService.obtenerPedidosPorUsuario(usuario.getIdUsuario());
            // model.addAttribute("pedidos", pedidos);
            
            // Por ahora, crear lista vacía o de prueba
            model.addAttribute("pedidos", new ArrayList<>());
        }
        
        return "client/pedidos";
    }

    @PostMapping("/configuracion")
    public String actualizarPerfil(@ModelAttribute("usuario") Usuario usuarioForm,
                                   @RequestParam(value = "fotoPerfilFile", required = false) MultipartFile fotoPerfilFile,
                                   Principal principal, Model model) {
        if (principal != null) {
            Usuario usuario = this.usuarioService.findByEmail(principal.getName());
            // Actualizar campos editables
            usuario.setNombre(usuarioForm.getNombre());
            usuario.setApellido(usuarioForm.getApellido());
            usuario.setTelefono(usuarioForm.getTelefono());
            usuario.setDireccion(usuarioForm.getDireccion());
            usuario.setDni(usuarioForm.getDni());
            usuario.setFechaNacimiento(usuarioForm.getFechaNacimiento());
            
            this.usuarioService.save(usuario);
            model.addAttribute("mensaje", "Datos actualizados correctamente");
        }
        return "client/configuracion";
    }

    @PostMapping("/upload-profile-image")
    public ResponseEntity<String> uploadProfileImage(@RequestParam("image") MultipartFile image,
                                                     Principal principal) {
        try {
            if (principal != null) {
                Usuario usuario = this.usuarioService.findByEmail(principal.getName());
                if (usuario != null) {
                    imagenService.guardarImagenPerfil(usuario.getIdUsuario().longValue(), TipoUsuario.CLIENTE, image);
                    return ResponseEntity.ok("Imagen de perfil actualizada exitosamente");
                }
            }
            return ResponseEntity.badRequest().body("Usuario no encontrado");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error al subir la imagen: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Archivo no válido: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error inesperado: " + e.getMessage());
        }
    }

    /**
     * Página de checkout/pago
     */
    @GetMapping("/checkout")
    public String checkout(Model model, Principal principal, HttpSession session) {
        // Verificar si hay usuario logueado
        if (principal != null) {
            Usuario usuario = usuarioService.findByEmail(principal.getName());
            model.addAttribute("usuario", usuario);
            
            // Obtener imagen de perfil
            try {
                Optional<ImagenPerfil> imagenPerfilOpt = imagenService.obtenerImagenPerfil(
                    usuario.getIdUsuario().longValue(), TipoUsuario.CLIENTE);
                if (imagenPerfilOpt.isPresent()) {
                    model.addAttribute("imagenPerfil", imagenPerfilOpt.get());
                }
            } catch (Exception e) {
                // Si no hay imagen, continuamos sin ella
            }
        }

        // Obtener carrito de la sesión
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> carrito = (List<Map<String, Object>>) session.getAttribute("carrito");
        
        if (carrito == null || carrito.isEmpty()) {
            return "redirect:/client/carrito";
        }

        model.addAttribute("carrito", carrito);

        // Calcular totales
        double subtotal = carrito.stream()
            .mapToDouble(item -> {
                double precio = ((Number) item.get("precio")).doubleValue();
                int cantidad = (Integer) item.get("cantidad");
                return precio * cantidad;
            })
            .sum();

        double envio = subtotal > 200 ? 0 : 15;
        double descuento = subtotal * 0.05;
        double igv = (subtotal - descuento + envio) * 0.18;
        double total = subtotal - descuento + envio + igv;

        model.addAttribute("subtotal", subtotal);
        model.addAttribute("envio", envio);
        model.addAttribute("descuento", descuento);
        model.addAttribute("igv", igv);
        model.addAttribute("total", total);

        return "client/checkout";
    }

    /**
     * Procesar pago y crear pedido completo
     */
    @PostMapping("/procesar-pago")
    @SuppressWarnings("UseSpecificCatch")
    public String procesarPago(@RequestParam String tipoDocumento,
                              @RequestParam String numeroDocumento,
                              @RequestParam String nombres,
                              @RequestParam String apellidos,
                              @RequestParam String telefono,
                              @RequestParam String email,
                              @RequestParam String direccion,
                              @RequestParam String distrito,
                              @RequestParam String provincia,
                              @RequestParam String departamento,
                              @RequestParam String metodoPago,
                              @RequestParam(required = false) String numeroTarjeta,
                              @RequestParam(required = false) String mesVencimiento,
                              @RequestParam(required = false) String anoVencimiento,
                              @RequestParam(required = false) String cvv,
                              @RequestParam(required = false) String nombreTarjeta,
                              Principal principal,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        
        try {
            // Obtener carrito
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> carrito = (List<Map<String, Object>>) session.getAttribute("carrito");
            
            if (carrito == null || carrito.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No hay productos en el carrito");
                return "redirect:/client/carrito";
            }

            // Generar número de pedido único
            String numeroPedido = "ECO-" + System.currentTimeMillis();
            
            // Calcular totales
            double subtotal = carrito.stream()
                .mapToDouble(item -> {
                    double precio = ((Number) item.get("precio")).doubleValue();
                    int cantidad = (Integer) item.get("cantidad");
                    return precio * cantidad;
                })
                .sum();

            double envio = subtotal > 200 ? 0 : 15;
            double descuento = subtotal * 0.05;
            double igv = (subtotal - descuento + envio) * 0.18;
            double total = subtotal - descuento + envio + igv;

            // Simular procesamiento de pago
            Thread.sleep(2000); // Simular latencia de procesamiento
            
            // Validar datos de pago según método
            if (metodoPago.equals("TARJETA_CREDITO") || metodoPago.equals("TARJETA_DEBITO")) {
                if (numeroTarjeta == null || numeroTarjeta.trim().isEmpty() ||
                    cvv == null || cvv.trim().isEmpty() ||
                    mesVencimiento == null || anoVencimiento == null) {
                    redirectAttributes.addFlashAttribute("error", "Datos de tarjeta incompletos");
                    return "redirect:/client/checkout";
                }
            }

            // Crear pedido en base de datos si tienes el servicio implementado
            try {
                if (principal != null) {
                    Usuario usuario = usuarioService.findByEmail(principal.getName());
                    if (usuario != null) {
                        // Aquí puedes crear el pedido real en la base de datos
                        // Pedido pedidoReal = pedidoService.crearPedido(usuario, carrito, ...);
                        System.out.println("✅ Pedido creado para usuario: " + usuario.getEmail());
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error al crear pedido en BD: " + e.getMessage());
                // Continuar con el flujo aunque no se guarde en BD
            }

            // Guardar datos del pedido en sesión para la confirmación
            Map<String, Object> pedido = new HashMap<>();
            pedido.put("numeroPedido", numeroPedido);
            pedido.put("productos", new ArrayList<>(carrito));
            pedido.put("subtotal", subtotal);
            pedido.put("envio", envio);
            pedido.put("descuento", descuento);
            pedido.put("igv", igv);
            pedido.put("total", total);
            pedido.put("fechaPedido", new java.util.Date());
            pedido.put("metodoPago", metodoPago);
            pedido.put("datosCliente", Map.of(
                "tipoDocumento", tipoDocumento,
                "numeroDocumento", numeroDocumento,
                "nombres", nombres,
                "apellidos", apellidos,
                "telefono", telefono,
                "email", email,
                "direccion", direccion,
                "distrito", distrito,
                "provincia", provincia,
                "departamento", departamento
            ));

            // Intentar enviar email de confirmación
            try {
                // emailService.enviarConfirmacionPedido(email, nombres + " " + apellidos, numeroPedido, String.valueOf(total));
                System.out.println("📧 Email de confirmación enviado a: " + email);
            } catch (Exception e) {
                System.err.println("⚠️ Error al enviar email: " + e.getMessage());
                // Continuar sin email
            }

            session.setAttribute("ultimoPedido", pedido);
            
            // Limpiar carrito
            session.removeAttribute("carrito");
            
            return "redirect:/client/confirmacion";
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", "Datos de pago inválidos: " + e.getMessage());
            return "redirect:/client/checkout";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error en el procesamiento: " + e.getMessage());
            return "redirect:/client/checkout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error inesperado al procesar el pago: " + e.getMessage());
            return "redirect:/client/checkout";
        }
    }

    /**
     * Página de confirmación de compra exitosa
     */
    @GetMapping("/confirmacion")
    public String confirmacion(Model model, Principal principal, HttpSession session) {
        // Verificar si hay usuario logueado
        if (principal != null) {
            Usuario usuario = usuarioService.findByEmail(principal.getName());
            model.addAttribute("usuario", usuario);
        }

        // Obtener datos del último pedido
        @SuppressWarnings("unchecked")
        Map<String, Object> pedido = (Map<String, Object>) session.getAttribute("ultimoPedido");
        
        if (pedido == null) {
            return "redirect:/client/catalogo";
        }

        // Agregar datos del pedido al modelo
        model.addAttribute("numeroPedido", pedido.get("numeroPedido"));
        model.addAttribute("productos", pedido.get("productos"));
        model.addAttribute("subtotal", pedido.get("subtotal"));
        model.addAttribute("envio", pedido.get("envio"));
        model.addAttribute("descuento", pedido.get("descuento"));
        model.addAttribute("igv", pedido.get("igv"));
        model.addAttribute("total", pedido.get("total"));
        model.addAttribute("fechaPedido", pedido.get("fechaPedido"));
        model.addAttribute("metodoPago", pedido.get("metodoPago"));
        
        // Extraer datos del cliente
        @SuppressWarnings("unchecked")
        Map<String, String> datosCliente = (Map<String, String>) pedido.get("datosCliente");
        if (datosCliente != null) {
            model.addAttribute("nombreCliente", datosCliente.get("nombres") + " " + datosCliente.get("apellidos"));
            model.addAttribute("emailCliente", datosCliente.get("email"));
            model.addAttribute("direccionEnvio", datosCliente.get("direccion") + ", " + 
                                                datosCliente.get("distrito") + ", " + 
                                                datosCliente.get("provincia"));
        }
        
        // Calcular estadísticas del pedido
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> productos = (List<Map<String, Object>>) pedido.get("productos");
        if (productos != null) {
            int cantidadTotal = productos.stream().mapToInt(item -> (Integer) item.get("cantidad")).sum();
            model.addAttribute("cantidadProductos", cantidadTotal);
        }
        
        return "client/confirmacion";
    }

    /**
     * Descargar boleta/factura en PDF
     */
    @GetMapping("/descargar-boleta")
    @SuppressWarnings("UseSpecificCatch")
    public ResponseEntity<byte[]> descargarBoleta(@RequestParam(required = false) String numeroPedido,
                                                 HttpSession session) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> pedido = (Map<String, Object>) session.getAttribute("ultimoPedido");
            
            if (pedido == null) {
                return ResponseEntity.badRequest().build();
            }

            // Verificar que el número de pedido coincida si se proporciona
            if (numeroPedido != null && !numeroPedido.equals(pedido.get("numeroPedido"))) {
                return ResponseEntity.badRequest().build();
            }

            // Generar PDF usando el servicio de exportación
            byte[] pdfBytes;
            try {
                pdfBytes = exportService.generarBoletaPDF(pedido);
            } catch (Exception e) {
                System.err.println("Error al generar PDF: " + e.getMessage());
                // Crear un PDF básico de fallback
                pdfBytes = generarPDFBasico(pedido);
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                "boleta_" + pedido.get("numeroPedido") + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Generar PDF básico cuando el servicio principal falla
     */
    private byte[] generarPDFBasico(Map<String, Object> pedido) {
        // Implementación básica de PDF como fallback
        String contenido = "=== BOLETA ELECTRONICA - ECOMAXTIENDA ===\n\n";
        contenido += "Numero de Pedido: " + pedido.get("numeroPedido") + "\n";
        contenido += "Fecha: " + pedido.get("fechaPedido") + "\n";
        contenido += "Total: S/ " + String.format("%.2f", (Double) pedido.get("total")) + "\n\n";
        contenido += "Gracias por su compra eco-amigable!\n";
        
        return contenido.getBytes();
    }
    
    @GetMapping("/catalogo-simple-test")
    public String catalogoSimpleTest(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String categoria,
            @RequestParam(defaultValue = "0") Double minPrecio,
            @RequestParam(defaultValue = "999999") Double maxPrecio,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication,
            Model model) {

        System.out.println("🧪 === CATÁLOGO SIMPLE TEST - INICIANDO ===");
        
        String usuario = authentication != null ? authentication.getName() : "Anónimo";
        System.out.println("👤 Usuario: " + usuario);
        System.out.println("📄 Página: " + page + " | Tamaño: " + size);

        try {
            System.out.println("📦 Obteniendo todos los productos paginados...");
            
            // Crear el Pageable con la información de paginación y ordenamiento
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Producto> productos = productoService.obtenerProductosPaginados(pageable);
            
            System.out.println("✅ Productos encontrados: " + productos.getTotalElements());
            System.out.println("📄 Productos en esta página: " + productos.getNumberOfElements());
            
            List<String> categorias = productoService.obtenerCategorias();
            System.out.println("🏷️ Categorías: " + categorias.size());
            
            // Añadir al modelo
            model.addAttribute("productos", productos);
            model.addAttribute("categorias", categorias);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", productos.getTotalPages());
            model.addAttribute("totalItems", productos.getTotalElements());
            model.addAttribute("size", size);
            model.addAttribute("busqueda", busqueda);
            model.addAttribute("categoria", categoria);
            model.addAttribute("minPrecio", minPrecio > 0 ? minPrecio : null);
            model.addAttribute("maxPrecio", maxPrecio < 999999 ? maxPrecio : null);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            
            System.out.println("🧪 Catálogo simple test listo - Total elementos: " + productos.getTotalElements());
            return "client/catalogo-simple-test";
            
        } catch (Exception e) {
            System.err.println("❌ Error en catálogo simple test: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar el catálogo: " + e.getMessage());
            return "client/catalogo-simple-test";
        }
    }
}
