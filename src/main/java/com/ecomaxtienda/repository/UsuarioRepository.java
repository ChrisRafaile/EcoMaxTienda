package com.ecomaxtienda.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecomaxtienda.entity.Rol;
import com.ecomaxtienda.entity.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    // Buscar por email
    Optional<Usuario> findByEmail(String email);
    
    // Buscar por email ignorando mayúsculas
    Optional<Usuario> findByEmailIgnoreCase(String email);
    
    // Buscar usuario con rol por email
    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol WHERE LOWER(u.email) = LOWER(:email)")
    Optional<Usuario> findByEmailIgnoreCaseWithRole(@Param("email") String email);
    
    // Verificar si existe email
    boolean existsByEmail(String email);
    
    // Buscar usuarios activos
    List<Usuario> findByEstadoTrue();
    
    // Buscar usuarios por rol
    List<Usuario> findByRol(Rol rol);
    
    // Buscar usuarios por nombre de rol
    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre = :rolNombre")
    List<Usuario> findByRolNombre(@Param("rolNombre") String rolNombre);
    
    // Buscar administradores
    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre IN ('ADMIN', 'SUPER_ADMIN')")
    List<Usuario> findAdministradores();
    
    // Buscar clientes
    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre = 'CLIENTE'")
    List<Usuario> findClientes();
    
    // Buscar por nombre o apellido
    @Query("SELECT u FROM Usuario u WHERE CONCAT(u.nombre, ' ', u.apellido) LIKE %:nombreCompleto%")
    List<Usuario> findByNombreCompletoContaining(@Param("nombreCompleto") String nombreCompleto);
    
    // Búsqueda general
    @Query("SELECT u FROM Usuario u WHERE " +
           "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(u.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "u.telefono LIKE %:busqueda%")
    List<Usuario> buscarUsuarios(@Param("busqueda") String busqueda);
    
    // Usuarios registrados en un período
    @Query("SELECT u FROM Usuario u WHERE u.fechaRegistro BETWEEN :inicio AND :fin")
    List<Usuario> findByFechaRegistroBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
    
    // Usuarios con paginación
    Page<Usuario> findByEstadoTrue(Pageable pageable);
    
    // Contar usuarios por rol
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol.nombre = :rolNombre")
    Long contarPorRol(@Param("rolNombre") String rolNombre);
    
    // Estadísticas de usuarios
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.estado = true")
    Long contarUsuariosActivos();
    
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.fechaRegistro >= :fecha")
    Long contarUsuariosRegistradosDespueDe(@Param("fecha") LocalDateTime fecha);
    
    // Últimos usuarios registrados
    @Query("SELECT u FROM Usuario u ORDER BY u.fechaRegistro DESC")
    List<Usuario> findUltimosRegistrados(Pageable pageable);
}
