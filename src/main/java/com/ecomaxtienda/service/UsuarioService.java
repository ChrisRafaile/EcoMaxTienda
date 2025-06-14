package com.ecomaxtienda.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecomaxtienda.entity.Rol;
import com.ecomaxtienda.entity.Usuario;
import com.ecomaxtienda.repository.RolRepository;
import com.ecomaxtienda.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // Métodos CRUD básicos
    public List<Usuario> findAll() {
        return this.usuarioRepository.findAll();
    }
    
    public Optional<Usuario> findById(Integer id) {
        return this.usuarioRepository.findById(id);
    }
    
    public Usuario save(Usuario usuario) {
        return this.usuarioRepository.save(usuario);
    }
    
    public void deleteById(Integer id) {
        this.usuarioRepository.deleteById(id);
    }
    
    // Métodos de búsqueda
    public Optional<Usuario> buscarPorEmail(String email) {
        return this.usuarioRepository.findByEmail(email);
    }
    
    // Método de compatibilidad para ClientController
    public Usuario findByEmail(String email) {
        return this.usuarioRepository.findByEmail(email).orElse(null);
    }
    
    public Optional<Usuario> buscarPorEmailConRol(String email) {
        return this.usuarioRepository.findByEmailIgnoreCaseWithRole(email);
    }
    
    public boolean existeEmail(String email) {
        return this.usuarioRepository.existsByEmail(email);
    }
    
    // Métodos de negocio
    public Usuario crearUsuario(String nombre, String apellido, String email, String password, String rolNombre) {
        if (this.existeEmail(email)) {
            throw new RuntimeException("Ya existe un usuario con el correo electrónico: " + email);
        }
        
        Optional<Rol> rol = this.rolRepository.findByNombre(rolNombre);
        if (rol.isEmpty()) {
            throw new RuntimeException("Rol no encontrado: " + rolNombre);
        }
        
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail(email.toLowerCase());
        usuario.setPassword(this.passwordEncoder.encode(password));
        usuario.setRol(rol.get());
        usuario.setEstado(true);
        usuario.setFechaRegistro(LocalDateTime.now());
        
        Usuario usuarioGuardado = this.usuarioRepository.save(usuario);
          // Enviar email de bienvenida
        try {
            if ("CLIENTE".equals(rolNombre)) {
                this.emailService.enviarCorreoBienvenida(usuarioGuardado.getEmail(), usuarioGuardado.getNombreCompleto());
            } else if ("ADMIN".equals(rolNombre) || "SUPER_ADMIN".equals(rolNombre)) {
                // Generar credenciales temporales para admin
                String credencialesTemp = "Admin" + System.currentTimeMillis() + "!";
                this.emailService.enviarCorreoBienvenidaAdmin(usuarioGuardado.getEmail(), usuarioGuardado.getNombreCompleto(), rolNombre, credencialesTemp);
            }
        } catch (Exception e) {
            // Log error but don't fail user creation
            System.err.println("Error enviando correo electrónico de bienvenida: " + e.getMessage());
        }
        
        return usuarioGuardado;
    }
    
    public Usuario crearCliente(String nombre, String apellido, String email, String password) {
        return this.crearUsuario(nombre, apellido, email, password, "CLIENTE");
    }
    
    public Usuario crearAdmin(String nombre, String apellido, String email, String password, String telefono, String direccion) {
        Usuario usuario = this.crearUsuario(nombre, apellido, email, password, "ADMIN");
        usuario.setTelefono(telefono);
        usuario.setDireccion(direccion);
        return this.usuarioRepository.save(usuario);
    }
    
    public Usuario actualizarUsuario(Integer id, String nombre, String apellido, String telefono, String direccion) {
        Optional<Usuario> usuarioExistente = this.usuarioRepository.findById(id);
        if (usuarioExistente.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        
        Usuario usuario = usuarioExistente.get();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setTelefono(telefono);
        usuario.setDireccion(direccion);
        
        return this.usuarioRepository.save(usuario);
    }
    
    public void cambiarPassword(Integer id, String passwordActual, String nuevaPassword) {
        Optional<Usuario> usuarioExistente = this.usuarioRepository.findById(id);
        if (usuarioExistente.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        
        Usuario usuario = usuarioExistente.get();
        
        if (!this.passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }
        
        usuario.setPassword(this.passwordEncoder.encode(nuevaPassword));
        this.usuarioRepository.save(usuario);
    }
    
    public void desactivarUsuario(Integer id) {
        Optional<Usuario> usuarioExistente = this.usuarioRepository.findById(id);
        if (usuarioExistente.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        
        Usuario usuario = usuarioExistente.get();
        usuario.setEstado(false);
        this.usuarioRepository.save(usuario);
    }
    
    public void activarUsuario(Integer id) {
        Optional<Usuario> usuarioExistente = this.usuarioRepository.findById(id);
        if (usuarioExistente.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        
        Usuario usuario = usuarioExistente.get();
        usuario.setEstado(true);
        this.usuarioRepository.save(usuario);
    }
    
    public void actualizarUltimoAcceso(Integer id) {
        Optional<Usuario> usuarioExistente = this.usuarioRepository.findById(id);
        if (usuarioExistente.isPresent()) {
            Usuario usuario = usuarioExistente.get();
            usuario.setUltimoAcceso(LocalDateTime.now());
            this.usuarioRepository.save(usuario);
        }
    }
    
    // Consultas específicas
    public List<Usuario> obtenerUsuariosActivos() {
        return this.usuarioRepository.findByEstadoTrue();
    }
    
    public List<Usuario> obtenerAdministradores() {
        return this.usuarioRepository.findAdministradores();
    }
    
    public List<Usuario> obtenerClientes() {
        return this.usuarioRepository.findClientes();
    }
    
    public List<Usuario> buscarUsuarios(String busqueda) {
        return this.usuarioRepository.buscarUsuarios(busqueda);
    }
    
    public Page<Usuario> obtenerUsuariosPaginados(Pageable pageable) {
        return this.usuarioRepository.findByEstadoTrue(pageable);
    }
    
    // Estadísticas
    public Long contarUsuariosActivos() {
        return this.usuarioRepository.contarUsuariosActivos();
    }
    
    public Long contarClientes() {
        return this.usuarioRepository.contarPorRol("CLIENTE");
    }
    
    public Long contarAdministradores() {
        return this.usuarioRepository.contarPorRol("ADMIN") + this.usuarioRepository.contarPorRol("SUPER_ADMIN");
    }
    
    public Long contarUsuariosRegistradosHoy() {
        LocalDateTime inicioHoy = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return this.usuarioRepository.contarUsuariosRegistradosDespueDe(inicioHoy);
    }
    
    public List<Usuario> obtenerUltimosRegistrados(int cantidad) {
        return this.usuarioRepository.findUltimosRegistrados(Pageable.ofSize(cantidad));
    }
    
    // Validaciones
    public boolean puedeEliminarUsuario(Integer id) {
        Optional<Usuario> usuario = this.usuarioRepository.findById(id);
        if (usuario.isEmpty()) {
            return false;
        }
        
        // No se puede eliminar si tiene pedidos
        return usuario.get().getPedidos() == null || usuario.get().getPedidos().isEmpty();
    }
    
    public boolean esAdministrador(Usuario usuario) {
        return usuario.isAdmin();
    }
    
    public boolean esCliente(Usuario usuario) {
        return usuario.isCliente();
    }
    
    // Métodos adicionales para gestión de administradores
    public Usuario actualizarUsuario(Usuario usuario) {
        return this.usuarioRepository.save(usuario);
    }
    
    public void cambiarPassword(String email, String passwordActual, String passwordNueva) {
        Usuario usuario = this.buscarPorEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (!this.passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }
        
        if (passwordNueva.length() < 8) {
            throw new RuntimeException("La nueva contraseña debe tener al menos 8 caracteres");
        }
        
        usuario.setPassword(this.passwordEncoder.encode(passwordNueva));
        this.usuarioRepository.save(usuario);
    }
}
