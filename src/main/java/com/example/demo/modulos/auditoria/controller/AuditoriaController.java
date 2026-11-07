package com.example.demo.modulos.auditoria.controller;

import com.example.demo.modulos.auditoria.entity.AuditoriaEvento;
import com.example.demo.modulos.auditoria.repository.AuditoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/auditoria")
public class AuditoriaController {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaController(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String accion,
                         @RequestParam(required = false) String entidad,
                         @RequestParam(required = false) String actor,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                         @RequestParam(required = false, name = "q") String q,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        String accionFiltro = limpiar(accion);
        String entidadFiltro = limpiar(entidad);
        String actorFiltro = limpiar(actor);
        String texto = limpiar(q);
        LocalDateTime desdeFecha = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaFecha = hasta != null ? hasta.plusDays(1).atStartOfDay().minusNanos(1) : null;

        int paginaActual = Math.max(page, 0);
        Page<AuditoriaEvento> pagina = auditoriaRepository.buscar(
                accionFiltro,
                entidadFiltro,
                actorFiltro != null ? "%" + actorFiltro.toLowerCase() + "%" : null,
                desdeFecha,
                hastaFecha,
                texto != null ? "%" + texto.toLowerCase() + "%" : null,
                PageRequest.of(paginaActual, 20, Sort.by("fecha").descending()));

        model.addAttribute("eventos", pagina.getContent());
        model.addAttribute("pagina", pagina);
        model.addAttribute("paginaActual", paginaActual);
        model.addAttribute("acciones", auditoriaRepository.accionesDistintas());
        model.addAttribute("entidades", auditoriaRepository.entidadesDistintas());
        model.addAttribute("filtroAccion", accionFiltro);
        model.addAttribute("filtroEntidad", entidadFiltro);
        model.addAttribute("filtroActor", actorFiltro);
        model.addAttribute("filtroDesde", desde);
        model.addAttribute("filtroHasta", hasta);
        model.addAttribute("filtroQ", texto);
        return "auditoria/lista";
    }

    private String limpiar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}
