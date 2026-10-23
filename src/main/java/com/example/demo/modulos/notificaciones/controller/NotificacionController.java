package com.example.demo.modulos.notificaciones.controller;

import com.example.demo.modulos.notificaciones.entity.Notificacion;
import com.example.demo.modulos.notificaciones.repository.NotificacionRepository;
import com.example.demo.modulos.notificaciones.service.NotificacionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final NotificacionRepository notificacionRepository;

    public NotificacionController(NotificacionService notificacionService,
                                  NotificacionRepository notificacionRepository) {
        this.notificacionService = notificacionService;
        this.notificacionRepository = notificacionRepository;
    }

    @GetMapping
    public String listar(Authentication authentication, Model model) {
        model.addAttribute("notificaciones",
                notificacionService.listar(authentication.getName()));
        return "notificaciones/lista";
    }

    @GetMapping("/{id}/abrir")
    public String abrir(@PathVariable Long id, Authentication authentication) {
        Notificacion notificacion = notificacionRepository
                .findByIdAndUsuarioUsername(id, authentication.getName())
                .orElse(null);
        if (notificacion == null) {
            return "redirect:/notificaciones";
        }
        notificacionService.marcarLeida(id, authentication.getName());
        String enlace = notificacion.getEnlace();
        if (enlace == null || enlace.isBlank() || !enlace.startsWith("/")) {
            return "redirect:/notificaciones";
        }
        return "redirect:" + enlace;
    }

    @PostMapping("/leer-todas")
    public String leerTodas(Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        notificacionService.marcarTodasLeidas(authentication.getName());
        redirectAttributes.addFlashAttribute("mensaje", "Notificaciones marcadas como leídas.");
        return "redirect:/notificaciones";
    }
}
