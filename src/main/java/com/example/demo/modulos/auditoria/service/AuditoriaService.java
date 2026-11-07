package com.example.demo.modulos.auditoria.service;

import com.example.demo.modulos.auditoria.entity.AuditoriaEvento;
import com.example.demo.modulos.auditoria.repository.AuditoriaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Transactional
    public void registrar(String accion, String entidad, Object entidadId, String detalle) {
        AuditoriaEvento evento = new AuditoriaEvento();
        evento.setFecha(LocalDateTime.now());
        evento.setActor(actorActual());
        evento.setAccion(accion);
        evento.setEntidad(entidad);
        evento.setEntidadId(entidadId != null ? String.valueOf(entidadId) : null);
        evento.setDetalle(detalle);
        auditoriaRepository.save(evento);
    }

    private String actorActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return "sistema";
        }
        return authentication.getName();
    }
}
