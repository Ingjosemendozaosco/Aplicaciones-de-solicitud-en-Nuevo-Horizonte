package com.example.demo.modulos.solicitudes.controller;

import com.example.demo.modulos.documentos.entity.Documento;
import com.example.demo.modulos.documentos.service.DocumentoService;
import com.example.demo.modulos.solicitudes.service.SolicitudService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/disponibles")
public class DisponiblesController {

    private final DocumentoService documentoService;
    private final SolicitudService solicitudService;

    public DisponiblesController(DocumentoService documentoService,
                                 SolicitudService solicitudService) {
        this.documentoService = documentoService;
        this.solicitudService = solicitudService;
    }

    @GetMapping
    public String ver(Model model) {
        var documentos = documentoService.listarTodos();
        var modulos = solicitudService.modulosDisponibles();
        var areas = solicitudService.areasDisponibles(modulos);
        model.addAttribute("documentos", documentos);
        model.addAttribute("documentosActivos", documentos.stream()
                .filter(Documento::isActivo).count());
        model.addAttribute("modulos", modulos);
        model.addAttribute("areas", areas);
        model.addAttribute("totalAreas", areas.size());
        return "disponibles/index";
    }

    @GetMapping("/{id}/archivo")
    public ResponseEntity<byte[]> archivo(@PathVariable Long id) {
        Documento documento = documentoService.buscar(id);
        String nombre = documento.getNombreArchivo() != null
                ? documento.getNombreArchivo() : "documento";
        String codificado = URLEncoder.encode(nombre, StandardCharsets.UTF_8).replace("+", "%20");
        MediaType mediaType = MediaType.parseMediaType(documento.getTipoArchivo() != null
                ? documento.getTipoArchivo() : MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename*=UTF-8''" + codificado)
                .body(documento.getContenido());
    }
}
