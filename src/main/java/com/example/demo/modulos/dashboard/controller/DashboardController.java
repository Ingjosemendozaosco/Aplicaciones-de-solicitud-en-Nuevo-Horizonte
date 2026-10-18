package com.example.demo.modulos.dashboard.controller;

import com.example.demo.modulos.gestionUsuariosRolesPermisos.service.RolService;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.service.UsuarioService;
import com.example.demo.modulos.solicitudes.dto.SolicitudResponseDTO;
import com.example.demo.modulos.solicitudes.entity.EstadoSolicitud;
import com.example.demo.modulos.solicitudes.service.SolicitudService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final SolicitudService solicitudService;
    private final UsuarioService usuarioService;
    private final RolService rolService;

    public DashboardController(SolicitudService solicitudService,
                               UsuarioService usuarioService,
                               RolService rolService) {
        this.solicitudService = solicitudService;
        this.usuarioService = usuarioService;
        this.rolService = rolService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        boolean administracion = tieneRol(authentication, "Administra");
        boolean gerencia = tieneRol(authentication, "Gerencia");
        boolean visionGlobal = administracion || gerencia;

        List<SolicitudResponseDTO> solicitudes = visionGlobal
                ? solicitudService.listarTodas()
                : solicitudService.listarPorUsuario(authentication.getName());

        model.addAttribute("totalSolicitudes", solicitudes.size());
        model.addAttribute("pendientes", contar(solicitudes, EstadoSolicitud.PENDIENTE));
        model.addAttribute("aprobadas", contar(solicitudes, EstadoSolicitud.APROBADA));
        model.addAttribute("rechazadas", contar(solicitudes, EstadoSolicitud.RECHAZADA));
        model.addAttribute("visionGlobal", visionGlobal);
        model.addAttribute("recientes", recientes(solicitudes));

        if (administracion) {
            model.addAttribute("totalUsuarios", usuarioService.total());
            model.addAttribute("totalRoles", rolService.total());
        }
        return "dashboard";
    }

    @GetMapping("/reportes")
    public String reportes(Authentication authentication, Model model) {
        boolean administracion = tieneRol(authentication, "Administra");
        boolean gerencia = tieneRol(authentication, "Gerencia");
        if (!administracion && !gerencia) {
            throw new org.springframework.security.access.AccessDeniedException("Sin permisos");
        }

        List<SolicitudResponseDTO> solicitudes = solicitudService.listarTodas();

        Map<String, Long> porEstado = new LinkedHashMap<>();
        porEstado.put("Pendientes", contar(solicitudes, EstadoSolicitud.PENDIENTE));
        porEstado.put("Aprobadas", contar(solicitudes, EstadoSolicitud.APROBADA));
        porEstado.put("Rechazadas", contar(solicitudes, EstadoSolicitud.RECHAZADA));

        Map<String, Long> porArea = solicitudes.stream()
                .collect(Collectors.groupingBy(SolicitudResponseDTO::getAreaSolicitante,
                        TreeMap::new, Collectors.counting()));

        model.addAttribute("totalSolicitudes", solicitudes.size());
        model.addAttribute("pendientes", contar(solicitudes, EstadoSolicitud.PENDIENTE));
        model.addAttribute("aprobadas", contar(solicitudes, EstadoSolicitud.APROBADA));
        model.addAttribute("rechazadas", contar(solicitudes, EstadoSolicitud.RECHAZADA));
        model.addAttribute("porEstado", porEstado);
        model.addAttribute("porArea", porArea);
        model.addAttribute("solicitudes", recientes(solicitudes));
        return "reportes";
    }

    private long contar(List<SolicitudResponseDTO> solicitudes, EstadoSolicitud estado) {
        return solicitudes.stream().filter(s -> s.getEstado() == estado).count();
    }

    private List<SolicitudResponseDTO> recientes(List<SolicitudResponseDTO> solicitudes) {
        return solicitudes.stream()
                .sorted(Comparator.comparing(SolicitudResponseDTO::getFechaCreacion,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .toList();
    }

    private boolean tieneRol(Authentication authentication, String fragmento) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains(fragmento));
    }
}
