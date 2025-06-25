package com.ecomaxtienda.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecomaxtienda.entity.Usuario;
import com.ecomaxtienda.service.UsuarioService;

@Controller
@RequestMapping("/client")
public class ClientController {

    private final UsuarioService usuarioService;

    public ClientController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        if (principal != null) {
            Usuario usuario = this.usuarioService.findByEmail(principal.getName());
            model.addAttribute("usuario", usuario);
        }
        return "client/home";
    }

    @GetMapping("/catalogo")
    public String catalogo(Model model, Principal principal) {
        if (principal != null) {
            Usuario usuario = this.usuarioService.findByEmail(principal.getName());
            model.addAttribute("usuario", usuario);
        }
        // Aquí agregarás la lógica para obtener productos
        return "client/catalogo";
    }

    @GetMapping("/suscripcion")
    public String suscripcion(Model model, Principal principal) {
        if (principal != null) {
            Usuario usuario = this.usuarioService.findByEmail(principal.getName());
            model.addAttribute("usuario", usuario);
        }
        return "client/suscripcion";
    }

    @GetMapping("/carrito")
    public String carrito(Model model, Principal principal) {
        if (principal != null) {
            Usuario usuario = this.usuarioService.findByEmail(principal.getName());
            model.addAttribute("usuario", usuario);
        }
        return "client/carrito";
    }

    @GetMapping("/pago")
    public String pago(Model model, Principal principal) {
        if (principal != null) {
            Usuario usuario = this.usuarioService.findByEmail(principal.getName());
            model.addAttribute("usuario", usuario);
        }
        return "client/pago";
    }

    @GetMapping("/confirmacion")
    public String confirmacion(Model model, Principal principal) {
        if (principal != null) {
            Usuario usuario = this.usuarioService.findByEmail(principal.getName());
            model.addAttribute("usuario", usuario);
        }
        return "client/confirmacion";
    }

    @GetMapping("/perfil")
    public String perfil(Model model, Principal principal) {
        if (principal != null) {
            Usuario usuario = this.usuarioService.findByEmail(principal.getName());
            model.addAttribute("usuario", usuario);
        }
        return "client/perfil";
    }

    @GetMapping("/configuracion")
    public String configuracion(Model model, Principal principal) {
        if (principal != null) {
            Usuario usuario = this.usuarioService.findByEmail(principal.getName());
            model.addAttribute("usuario", usuario);
        }
        return "client/configuracion";
    }

    @GetMapping("/pedidos")
    public String pedidos(Model model, Principal principal) {
        if (principal != null) {
            Usuario usuario = this.usuarioService.findByEmail(principal.getName());
            model.addAttribute("usuario", usuario);
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
            // Guardar foto de perfil si se subió
            if (fotoPerfilFile != null && !fotoPerfilFile.isEmpty()) {
                try {
                    String uploadsDir = "uploads/";
                    Path uploadsPath = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "static", uploadsDir);
                    if (!Files.exists(uploadsPath)) {
                        Files.createDirectories(uploadsPath);
                    }
                    String filename = usuario.getIdUsuario() + "_" + fotoPerfilFile.getOriginalFilename();
                    Path filePath = uploadsPath.resolve(filename);
                    fotoPerfilFile.transferTo(filePath);
                    usuario.setFotoPerfil(filename);
                } catch (IOException | IllegalStateException e) {
                    model.addAttribute("error", "Error al subir la foto de perfil: " + e.getMessage());
                }
            }
            this.usuarioService.save(usuario);
            // Recargar usuario actualizado desde la base de datos
            usuario = this.usuarioService.findByEmail(principal.getName());
            model.addAttribute("usuario", usuario);
            model.addAttribute("mensaje", "Datos actualizados correctamente");
        }
        return "client/configuracion";
    }
}
