package com.example.demo.modulos.documentos.controller;

import com.example.demo.modulos.documentos.entity.Documento;
import com.example.demo.modulos.documentos.service.DocumentoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/documentos")
public class DocumentoController {

    private final DocumentoService documentoService;

    public DocumentoController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("documentos", documentoService.listarTodos());
        return "documentos/lista";
    }

    @PostMapping
    public String subir(@RequestParam String nombre,
                        @RequestParam(required = false) String descripcion,
                        @RequestParam(required = false) String categoria,
                        @RequestParam MultipartFile archivo,
                        Authentication authentication,
                        RedirectAttributes redirectAttributes) {
        try {
            documentoService.subir(nombre, descripcion, categoria, archivo,
                    authentication.getName());
            redirectAttributes.addFlashAttribute("mensaje", "Documento publicado correctamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "No se pudo guardar el documento. Verifica el archivo e intenta nuevamente.");
        }
        return "redirect:/documentos";
    }

    @GetMapping("/{id}/archivo")
    public ResponseEntity<byte[]> descargar(@PathVariable Long id) {
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

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam boolean activo,
                                RedirectAttributes redirectAttributes) {
        try {
            documentoService.cambiarEstado(id, activo);
            redirectAttributes.addFlashAttribute("mensaje", activo
                    ? "Documento activado: ya está disponible para las solicitudes."
                    : "Documento desactivado: ya no aparecerá en nuevas solicitudes.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/documentos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            documentoService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Documento retirado del catálogo.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/documentos";
    }
}
