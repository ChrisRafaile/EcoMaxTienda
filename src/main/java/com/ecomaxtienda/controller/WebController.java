package com.ecomaxtienda.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class WebController {
  

    @GetMapping("/clientes")
    public String clientes(Model model) {
        model.addAttribute("pageTitle", "Gestión de Clientes");
        return "admin/clientes";
    }

    @GetMapping("/registro-admin")
    public String registroAdmin(Model model) {
        model.addAttribute("pageTitle", "Registro de Administrador");
        return "admin/registro-admin";
    }

    @GetMapping("/pedidos")
    public String pedidos(Model model) {
        model.addAttribute("pageTitle", "Gestión de Pedidos");
        return "admin/pedidos";
    }

    @GetMapping("/inventario")
    public String inventario(Model model) {
        model.addAttribute("pageTitle", "Gestión de Inventario");
        return "admin/inventario";
    }    @GetMapping("/reportes")
    public String reportes(Model model) {
        model.addAttribute("pageTitle", "Reportes y Estadísticas");
        return "admin/reportes";
    }

    @GetMapping("/configuracion")
    public String configuracion(Model model) {
        model.addAttribute("pageTitle", "Configuración del Sistema");
        return "admin/configuracion";
    }
}
