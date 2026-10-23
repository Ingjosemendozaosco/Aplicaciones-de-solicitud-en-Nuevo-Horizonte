package com.example.demo.modulos.solicitudes.controller;

import com.example.demo.modulos.notificaciones.service.NotificacionService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NotificacionesAdvice {

    private final NotificacionService notificacionService;

    public NotificacionesAdvice(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @ModelAttribute("notificacionesNoLeidas")
    public long notificacionesNoLeidas(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return 0;
        }
        return notificacionService.contarNoLeidas(authentication.getName());
    }
}
