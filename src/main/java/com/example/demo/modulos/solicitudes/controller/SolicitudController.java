package com.example.demo.modulos.solicitudes.controller;

import com.example.demo.modulos.solicitudes.dto.SolicitudDecisionDTO;
import com.example.demo.modulos.solicitudes.dto.SolicitudRequestDTO;
import com.example.demo.modulos.solicitudes.entity.Solicitud;
import com.example.demo.modulos.solicitudes.service.SolicitudService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/solicitudes")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @GetMapping("/nueva")
    public String formulario(Model model, Authentication authentication) {
        SolicitudRequestDTO solicitud = new SolicitudRequestDTO();
        solicitud.setFechaSolicitud(java.time.LocalDate.now());
        solicitud.setResponsableAprobacion("Administración");
        String areaDefault = obtenerAreaPorUsuario(authentication);
        if (areaDefault != null && !areaDefault.isBlank()) {
            solicitud.setAreaSolicitante(areaDefault);
        }
        model.addAttribute("solicitud", solicitud);
        model.addAttribute("areas", List.of("Area de Producto", "Area de Ventas", "Gerencia"));
        return "solicitudes/formulario";
    }

    @PostMapping
    public String crear(@ModelAttribute("solicitud") SolicitudRequestDTO dto,
                        Authentication authentication,
                        RedirectAttributes redirectAttributes) {
        try {
            var creada = solicitudService.crear(dto, authentication.getName());
            redirectAttributes.addFlashAttribute("mensaje",
                    "Solicitud " + creada.getNumeroSolicitud() + " registrada correctamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/solicitudes/nueva";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error al registrar la solicitud. Verifica los datos e intenta nuevamente.");
            return "redirect:/solicitudes/nueva";
        }
        return "redirect:/solicitudes";
    }

    @GetMapping
    public String listar(Authentication authentication, Model model) {
        boolean administracion = esAdministracion(authentication);
        model.addAttribute("solicitudes", administracion
                ? solicitudService.listarTodas()
                : solicitudService.listarPorUsuario(authentication.getName()));
        model.addAttribute("puedeCrear", !administracion);
        return "solicitudes/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Authentication authentication, Model model) {
        model.addAttribute("solicitud", solicitudService.buscarPorId(id));
        model.addAttribute("decision", new SolicitudDecisionDTO());
        model.addAttribute("puedeDecidir", esAdministracion(authentication));
        return "solicitudes/detalle";
    }

    private boolean esAdministracion(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("Administra"));
    }

    private String obtenerAreaPorUsuario(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        return authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .filter(n -> n.equals("Area de Producto") || n.equals("Area de Ventas") || n.equals("Gerencia"))
                .findFirst()
                .orElse(null);
    }

    @PostMapping("/{id}/decision")
    public String decidir(@PathVariable Long id,
                          @ModelAttribute("decision") SolicitudDecisionDTO decision,
                          RedirectAttributes redirectAttributes) {
        try {
            solicitudService.decidir(id, decision.getEstado(), decision.getComentario());
            redirectAttributes.addFlashAttribute("mensaje", "Solicitud actualizada y notificada correctamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/solicitudes/" + id;
    }

    @GetMapping("/{id}/archivo")
    public ResponseEntity<byte[]> descargarArchivo(@PathVariable Long id) {
        Solicitud solicitud = solicitudService.buscarEntidadPorId(id);
        if (solicitud.getContenidoArchivo() == null || solicitud.getContenidoArchivo().length == 0) {
            return ResponseEntity.notFound().build();
        }
        String nombre = solicitud.getNombreArchivo() != null ? solicitud.getNombreArchivo() : "adjunto";
        String contenido = URLEncoder.encode(nombre, StandardCharsets.UTF_8).replace("+", "%20");
        MediaType mediaType = MediaType.parseMediaType(
                solicitud.getTipoArchivo() != null ? solicitud.getTipoArchivo() : MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename*=UTF-8''" + contenido)
                .body(solicitud.getContenidoArchivo());
    }
}
