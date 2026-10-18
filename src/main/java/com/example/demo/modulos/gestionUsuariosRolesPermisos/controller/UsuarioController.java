package com.example.demo.modulos.gestionUsuariosRolesPermisos.controller;

import com.example.demo.modulos.gestionUsuariosRolesPermisos.dto.UsuarioRequestDTO;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listar());
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setActivo(true);
        model.addAttribute("usuario", dto);
        model.addAttribute("roles", usuarioService.rolesDisponibles());
        model.addAttribute("edicion", false);
        return "usuarios/formulario";
    }

    @PostMapping
    public String crear(@ModelAttribute("usuario") UsuarioRequestDTO dto,
                        RedirectAttributes redirectAttributes) {
        try {
            usuarioService.crear(dto);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario creado correctamente");
            return "redirect:/usuarios";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/usuarios/nuevo";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", usuarioService.paraEdicion(id));
        model.addAttribute("roles", usuarioService.rolesDisponibles());
        model.addAttribute("edicion", true);
        return "usuarios/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @ModelAttribute("usuario") UsuarioRequestDTO dto,
                             RedirectAttributes redirectAttributes) {
        try {
            usuarioService.actualizar(id, dto);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado correctamente");
            return "redirect:/usuarios";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/usuarios/" + id + "/editar";
        }
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam boolean activo,
                                RedirectAttributes redirectAttributes) {
        try {
            usuarioService.cambiarEstado(id, activo);
            redirectAttributes.addFlashAttribute("mensaje",
                    activo ? "Usuario activado" : "Usuario desactivado");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/usuarios";
    }
}
