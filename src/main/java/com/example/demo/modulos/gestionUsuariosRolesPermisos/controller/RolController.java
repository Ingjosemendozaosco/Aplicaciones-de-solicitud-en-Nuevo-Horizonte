package com.example.demo.modulos.gestionUsuariosRolesPermisos.controller;

import com.example.demo.modulos.gestionUsuariosRolesPermisos.dto.PermisosFormDTO;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.dto.RolRequestDTO;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.service.RolService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/roles")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("roles", rolService.listar());
        return "roles/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("rol", new RolRequestDTO());
        model.addAttribute("edicion", false);
        return "roles/formulario";
    }

    @PostMapping
    public String crear(@ModelAttribute("rol") RolRequestDTO dto,
                        RedirectAttributes redirectAttributes) {
        try {
            rolService.crear(dto);
            redirectAttributes.addFlashAttribute("mensaje", "Rol creado correctamente");
            return "redirect:/roles";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/roles/nuevo";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("rol", rolService.paraEdicion(id));
        model.addAttribute("edicion", true);
        return "roles/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @ModelAttribute("rol") RolRequestDTO dto,
                             RedirectAttributes redirectAttributes) {
        try {
            rolService.actualizar(id, dto);
            redirectAttributes.addFlashAttribute("mensaje", "Rol actualizado correctamente");
            return "redirect:/roles";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/roles/" + id + "/editar";
        }
    }

    @GetMapping("/{id}/permisos")
    public String permisos(@PathVariable Long id, Model model) {
        model.addAttribute("rol", rolService.paraEdicion(id));
        model.addAttribute("form", rolService.formularioPermisos(id));
        return "roles/permisos";
    }

    @PostMapping("/{id}/permisos")
    public String guardarPermisos(@PathVariable Long id,
                                  @ModelAttribute("form") PermisosFormDTO form,
                                  RedirectAttributes redirectAttributes) {
        try {
            rolService.actualizarPermisos(id, form);
            redirectAttributes.addFlashAttribute("mensaje", "Permisos actualizados correctamente");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/roles";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            rolService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Rol eliminado");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/roles";
    }
}
