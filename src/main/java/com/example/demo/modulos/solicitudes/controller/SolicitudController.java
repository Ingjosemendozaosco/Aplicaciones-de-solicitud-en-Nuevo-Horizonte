package com.example.demo.modulos.solicitudes.controller;

import com.example.demo.modulos.ModuloCatalogo;
import com.example.demo.modulos.documentos.entity.Documento;
import com.example.demo.modulos.documentos.service.DocumentoService;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.service.CargoFormal;
import com.example.demo.modulos.solicitudes.dto.SolicitudDecisionDTO;
import com.example.demo.modulos.solicitudes.dto.SolicitudPropuestaDTO;
import com.example.demo.modulos.solicitudes.dto.SolicitudRequestDTO;
import com.example.demo.modulos.solicitudes.entity.EstadoSolicitud;
import com.example.demo.modulos.solicitudes.entity.Solicitud;
import com.example.demo.modulos.solicitudes.entity.TipoSolicitud;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/solicitudes")
public class SolicitudController {

    private static final Map<String, String> NIVELES_ACCESO = nivelesAcceso();
    private static final List<String> JERARQUIA = List.of(
            "Gerencia", "Administración", "Area de Ventas", "Area de Producto");

    private final SolicitudService solicitudService;
    private final DocumentoService documentoService;

    public SolicitudController(SolicitudService solicitudService,
                               DocumentoService documentoService) {
        this.solicitudService = solicitudService;
        this.documentoService = documentoService;
    }

    @GetMapping("/nueva")
    public String formulario(Model model, Authentication authentication) {
        SolicitudRequestDTO solicitud = new SolicitudRequestDTO();
        solicitud.setTipoSolicitud(TipoSolicitud.INFORMACION);
        solicitud.setNombreSolicitante(authentication.getName());
        solicitud.setFechaInicioAcceso(java.time.LocalDate.now());
        solicitud.setFechaFinAcceso(java.time.LocalDate.now().plusDays(30));
        String areaDefault = obtenerAreaPorUsuario(authentication);
        solicitud.setAreaSolicitante(areaDefault);
        List<String> rolesActuales = rolesDe(authentication);
        model.addAttribute("solicitud", solicitud);
        model.addAttribute("areaActual", areaDefault);
        model.addAttribute("jerarquia", JERARQUIA);
        model.addAttribute("rolesActuales", rolesActuales);
        model.addAttribute("cargoActual", CargoFormal.deNombres(rolesActuales));
        model.addAttribute("documentosDisponibles", documentoService.listar());
        model.addAttribute("modulos", ModuloCatalogo.etiquetas());
        model.addAttribute("niveles", NIVELES_ACCESO);
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

    @PostMapping("/borrador")
    public String guardarBorrador(@ModelAttribute("solicitud") SolicitudRequestDTO dto,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            var borrador = solicitudService.guardarBorrador(dto, authentication.getName());
            redirectAttributes.addFlashAttribute("mensaje",
                    "Borrador " + borrador.getNumeroSolicitud() + " guardado correctamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/solicitudes/nueva";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error al guardar el borrador. Verifica los datos e intenta nuevamente.");
            return "redirect:/solicitudes/nueva";
        }
        return "redirect:/solicitudes";
    }

    @GetMapping
    public String listar(Authentication authentication, Model model) {
        boolean visionGlobal = esAdministracion(authentication) || esGerencia(authentication);
        var solicitudes = visionGlobal
                ? solicitudService.listarTodas()
                : solicitudService.listarPorUsuario(authentication.getName());
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("puedeCrear", puedeCrear(authentication));
        model.addAttribute("total", solicitudes.size());
        model.addAttribute("totalPendientes", solicitudes.stream()
                .filter(s -> s.getEstado() == com.example.demo.modulos.solicitudes.entity.EstadoSolicitud.PENDIENTE)
                .count());
        model.addAttribute("totalRevision", solicitudes.stream()
                .filter(s -> s.getEstado() == com.example.demo.modulos.solicitudes.entity.EstadoSolicitud.EN_REVISION)
                .count());
        model.addAttribute("totalAprobadas", solicitudes.stream()
                .filter(s -> s.getEstado() == com.example.demo.modulos.solicitudes.entity.EstadoSolicitud.APROBADA)
                .count());
        model.addAttribute("totalRechazadas", solicitudes.stream()
                .filter(s -> s.getEstado() == com.example.demo.modulos.solicitudes.entity.EstadoSolicitud.RECHAZADA)
                .count());
        return "solicitudes/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Authentication authentication, Model model) {
        var solicitud = solicitudService.buscarPorId(id);
        boolean administracion = esAdministracion(authentication);
        boolean esSolicitante = authentication.getName().equals(solicitud.getUsernameSolicitante());
        if (!administracion && !esGerencia(authentication) && !esSolicitante) {
            return "redirect:/solicitudes";
        }
        if (administracion && solicitud.getEstado() == EstadoSolicitud.PENDIENTE
                && !solicitud.isVisto()) {
            solicitudService.marcarVisto(id);
            solicitud.setVisto(true);
        }
        model.addAttribute("solicitud", solicitud);
        SolicitudDecisionDTO decision = new SolicitudDecisionDTO();
        if (administracion && solicitud.getTipoSolicitud() == TipoSolicitud.INFORMACION) {
            List<Documento> documentos = documentoService.listar();
            model.addAttribute("documentos", documentos);
            documentos.stream()
                    .filter(doc -> coincideDocumentoSolicitado(
                            solicitud.getRecursoModulo(), doc.getNombre()))
                    .findFirst()
                    .ifPresent(doc -> decision.setIdDocumento(doc.getId()));
        }
        model.addAttribute("decision", decision);
        SolicitudPropuestaDTO propuesta = new SolicitudPropuestaDTO();
        propuesta.setFechaInicio(solicitud.getFechaInicioAcceso());
        propuesta.setFechaFin(solicitud.getFechaFinAcceso());
        propuesta.setPermanente(solicitud.isPermanente());
        model.addAttribute("propuesta", propuesta);
        model.addAttribute("puedeDecidir", administracion);
        model.addAttribute("esSolicitante", esSolicitante);
        if (administracion && solicitud.getTipoSolicitud() == TipoSolicitud.ACCESO) {
            model.addAttribute("modulosActuales", solicitudService.modulosActuales(id));
            model.addAttribute("rolesSolicitante", solicitud.getRolesSolicitante());
        }
        model.addAttribute("niveles", NIVELES_ACCESO);
        return "solicitudes/detalle";
    }

    private boolean esAdministracion(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("Administra"));
    }

    private boolean esGerencia(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("Gerencia"));
    }

    private boolean coincideDocumentoSolicitado(String recurso, String nombreDocumento) {
        if (recurso == null || nombreDocumento == null) {
            return false;
        }
        String solicitado = recurso.trim().toLowerCase();
        String nombre = nombreDocumento.trim().toLowerCase();
        return !nombre.isEmpty()
                && (solicitado.equals(nombre)
                || solicitado.startsWith(nombre + " - ")
                || solicitado.startsWith(nombre + "-"));
    }

    private boolean puedeCrear(Authentication authentication) {
        List<String> roles = rolesDe(authentication);
        return roles.contains("Area de Ventas") || roles.contains("Area de Producto");
    }

    private List<String> rolesDe(Authentication authentication) {
        if (authentication == null) {
            return List.of();
        }
        return authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .collect(Collectors.toList());
    }

    private String obtenerAreaPorUsuario(Authentication authentication) {
        List<String> roles = rolesDe(authentication);
        return roles.stream()
                .filter(JERARQUIA::contains)
                .findFirst()
                .orElse("Administración");
    }

    private static Map<String, String> nivelesAcceso() {
        Map<String, String> niveles = new LinkedHashMap<>();
        niveles.put("LECTURA", "Lectura");
        niveles.put("EDICION", "Edición");
        return niveles;
    }

    @PostMapping("/{id}/decision")
    public String decidir(@PathVariable Long id,
                          @ModelAttribute("decision") SolicitudDecisionDTO decision,
                          RedirectAttributes redirectAttributes) {
        try {
            solicitudService.decidir(id, decision.getEstado(), decision.getComentario(),
                    decision.getIdDocumento());
            redirectAttributes.addFlashAttribute("mensaje", "Solicitud actualizada y notificada correctamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo guardar la decisión. Verifica el archivo e intenta nuevamente.");
        }
        return "redirect:/solicitudes/" + id;
    }

    @PostMapping("/{id}/modulos/nivel")
    public String cambiarNivel(@PathVariable Long id,
                               @RequestParam Long idPermiso,
                               @RequestParam String nivel,
                               RedirectAttributes redirectAttributes) {
        try {
            solicitudService.cambiarNivelModulo(id, idPermiso, nivel);
            redirectAttributes.addFlashAttribute("mensaje", "Nivel de acceso actualizado");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/solicitudes/" + id;
    }

    @PostMapping("/{id}/modulos/revocar")
    public String revocar(@PathVariable Long id,
                          @RequestParam Long idPermiso,
                          RedirectAttributes redirectAttributes) {
        try {
            solicitudService.revocarModulo(id, idPermiso);
            redirectAttributes.addFlashAttribute("mensaje", "Acceso al módulo eliminado");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/solicitudes/" + id;
    }

    @PostMapping("/{id}/modulos/agregar")
    public String agregarAcceso(@PathVariable Long id,
                                @RequestParam String modulo,
                                @RequestParam Long idRol,
                                @RequestParam String nivel,
                                RedirectAttributes redirectAttributes) {
        try {
            solicitudService.agregarAcceso(id, modulo, idRol, nivel);
            redirectAttributes.addFlashAttribute("mensaje", "Acceso agregado correctamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/solicitudes/" + id;
    }

    @PostMapping("/{id}/propuesta")
    public String proponer(@PathVariable Long id,
                           @ModelAttribute("propuesta") SolicitudPropuestaDTO propuesta,
                           RedirectAttributes redirectAttributes) {
        try {
            solicitudService.proponer(id, propuesta);
            redirectAttributes.addFlashAttribute("mensaje",
                    "Propuesta enviada al solicitante para su aceptación");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/solicitudes/" + id;
    }

    @PostMapping("/{id}/propuesta/aceptar")
    public String aceptarPropuesta(@PathVariable Long id,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            solicitudService.aceptarPropuesta(id, authentication.getName());
            redirectAttributes.addFlashAttribute("mensaje", "Propuesta aceptada. Los accesos fueron actualizados.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/solicitudes/" + id;
    }

    @PostMapping("/{id}/propuesta/rechazar")
    public String rechazarPropuesta(@PathVariable Long id,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            solicitudService.rechazarPropuesta(id, authentication.getName());
            redirectAttributes.addFlashAttribute("mensaje", "Propuesta rechazada.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/solicitudes/" + id;
    }

    @GetMapping("/{id}/constancia")
    public String constancia(@PathVariable Long id, Authentication authentication,
                             Model model, RedirectAttributes redirectAttributes) {
        Solicitud solicitud = solicitudService.buscarEntidadPorId(id);
        boolean administracion = esAdministracion(authentication);
        boolean propietario = solicitud.getUsuario() != null
                && authentication.getName().equals(solicitud.getUsuario().getUsername());
        if (!administracion && !propietario) {
            redirectAttributes.addFlashAttribute("error", "No tienes acceso a esta constancia.");
            return "redirect:/solicitudes";
        }
        if (solicitud.getEstado() != EstadoSolicitud.APROBADA) {
            redirectAttributes.addFlashAttribute("error",
                    "La constancia solo está disponible para solicitudes aprobadas.");
            return "redirect:/solicitudes/" + id;
        }
        model.addAttribute("solicitud", solicitudService.buscarPorId(id));
        return "solicitudes/constancia";
    }

    @GetMapping("/{id}/archivo")
    public ResponseEntity<byte[]> descargarArchivo(@PathVariable Long id) {
        Solicitud solicitud = solicitudService.buscarEntidadPorId(id);
        return descargar(solicitud.getContenidoArchivo(), solicitud.getNombreArchivo(), solicitud.getTipoArchivo());
    }

    @GetMapping("/{id}/informe")
    public ResponseEntity<byte[]> descargarInforme(@PathVariable Long id) {
        Solicitud solicitud = solicitudService.buscarEntidadPorId(id);
        return descargar(solicitud.getContenidoInforme(), solicitud.getNombreInforme(), solicitud.getTipoInforme());
    }

    private ResponseEntity<byte[]> descargar(byte[] contenido, String nombreArchivo, String tipo) {
        if (contenido == null || contenido.length == 0) {
            return ResponseEntity.notFound().build();
        }
        String nombre = nombreArchivo != null ? nombreArchivo : "archivo";
        String contenidoCodificado = URLEncoder.encode(nombre, StandardCharsets.UTF_8).replace("+", "%20");
        MediaType mediaType = MediaType.parseMediaType(
                tipo != null ? tipo : MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename*=UTF-8''" + contenidoCodificado)
                .body(contenido);
    }
}
