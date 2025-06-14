package com.ecomaxtienda.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class LogoutController {

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        // Obtener la autenticación actual
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Si hay un usuario autenticado, cerrar la sesión
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, null, auth);
        }
        
        // Redirigir al login con mensaje de logout exitoso
        return "redirect:/auth/login?logout=success";
    }

    @PostMapping("/logout")
    public String logoutPost(HttpServletRequest request) {
        // Spring Security maneja automáticamente el POST logout
        return "redirect:/auth/login?logout=success";
    }
}
