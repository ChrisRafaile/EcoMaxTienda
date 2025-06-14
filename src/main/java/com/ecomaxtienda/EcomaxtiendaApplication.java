package com.ecomaxtienda;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecomaxtienda.entity.Rol;
import com.ecomaxtienda.entity.Usuario;
import com.ecomaxtienda.repository.RolRepository;
import com.ecomaxtienda.repository.UsuarioRepository;

@SpringBootApplication
public class EcomaxtiendaApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcomaxtiendaApplication.class, args);
	}

	@Bean
	@SuppressWarnings("unused")
	CommandLineRunner initData(RolRepository rolRepository, UsuarioRepository usuarioRepository, 
							   PasswordEncoder passwordEncoder) {
		return args -> {
			// Crear roles si no existen
			if (rolRepository.findByNombre("ROLE_ADMIN").isEmpty()) {
				Rol rolAdmin = new Rol();
				rolAdmin.setNombre("ROLE_ADMIN");
				rolAdmin.setDescripcion("Administrador del sistema");
				rolAdmin.setEstado(true);
				rolAdmin.setFechaCreacion(java.time.LocalDateTime.now());
				rolRepository.save(rolAdmin);
				System.out.println("Rol ROLE_ADMIN creado");
			}
			
			if (rolRepository.findByNombre("ROLE_CLIENTE").isEmpty()) {
				Rol rolCliente = new Rol();
				rolCliente.setNombre("ROLE_CLIENTE");
				rolCliente.setDescripcion("Cliente del sistema");
				rolCliente.setEstado(true);
				rolCliente.setFechaCreacion(java.time.LocalDateTime.now());
				rolRepository.save(rolCliente);
				System.out.println("Rol ROLE_CLIENTE creado");
			}
			
			// Crear usuario de prueba si no existe
			if (usuarioRepository.findByEmailIgnoreCase("test@test.com").isEmpty()) {
				Usuario usuario = new Usuario();
				usuario.setNombre("Usuario");
				usuario.setApellido("de Prueba");
				usuario.setEmail("test@test.com");
				usuario.setPassword(passwordEncoder.encode("123456"));
				usuario.setTelefono("123456789");
				usuario.setEstado(true);
				usuario.setRol(rolRepository.findByNombre("ROLE_CLIENTE").orElse(null));
				usuario.setFechaRegistro(java.time.LocalDateTime.now());
				usuarioRepository.save(usuario);
				System.out.println("Usuario de prueba creado: test@test.com / 123456");
			}
		};
	}
}
